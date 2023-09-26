/*
 * Copyright 2019-2020 Ernst Jan Plugge <rmc@dds.nl>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.smouldering_durtles.wk.activities

import android.app.SearchManager
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.res.Resources.Theme
import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.smouldering_durtles.wk.Actment
import com.smouldering_durtles.wk.Constants
import com.smouldering_durtles.wk.GlobalSettings
import com.smouldering_durtles.wk.Identification
import com.smouldering_durtles.wk.R
import com.smouldering_durtles.wk.WkApplication
import com.smouldering_durtles.wk.db.model.Subject
import com.smouldering_durtles.wk.enums.ActiveTheme
import com.smouldering_durtles.wk.enums.FragmentTransitionAnimation
import com.smouldering_durtles.wk.enums.SessionState
import com.smouldering_durtles.wk.enums.SessionType
import com.smouldering_durtles.wk.fragments.AbstractFragment
import com.smouldering_durtles.wk.jobs.ActivityResumedJob
import com.smouldering_durtles.wk.jobs.AutoSyncNowJob
import com.smouldering_durtles.wk.jobs.FlushTasksJob
import com.smouldering_durtles.wk.jobs.SettingChangedJob
import com.smouldering_durtles.wk.jobs.SyncNowJob
import com.smouldering_durtles.wk.jobs.TickJob
import com.smouldering_durtles.wk.livedata.LiveLevelDuration
import com.smouldering_durtles.wk.livedata.LiveSessionProgress
import com.smouldering_durtles.wk.livedata.LiveSessionState
import com.smouldering_durtles.wk.livedata.LiveTaskCounts
import com.smouldering_durtles.wk.model.LevelDuration
import com.smouldering_durtles.wk.model.Session
import com.smouldering_durtles.wk.model.TaskCounts
import com.smouldering_durtles.wk.services.JobRunnerService
import com.smouldering_durtles.wk.util.ObjectSupport
import com.smouldering_durtles.wk.util.TextUtil
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

/**
 * Abstract superclass for all activities that takes care of a bunch of common functionality.
 */
abstract class AbstractActivity protected constructor(
    private val layoutId: Int,
    private val optionsMenuId: Int
) : AppCompatActivity(), OnSharedPreferenceChangeListener, Actment {
    private val mainActivity: Boolean
    private var tickTimer: Timer? = null
    private var creationTheme: ActiveTheme? = null
    private var createdTheme: Theme? = null

    /**
     * True if the activity is 'active', i.e. the user can interact with the buttons on it.
     */
    @JvmField
    protected var interactionEnabled = false

    /**
     * The constructor.
     *
     * @param layoutId id for this activity's layout
     * @param optionsMenuId id for this activity's menu
     */
    init {
        mainActivity = this is MainActivity
    }

    private fun onCreateBaseLiveTaskCounts(actionBar: ActionBar, t: TaskCounts?) {
        val onlineStatus = WkApplication.getInstance().onlineStatus
        val hasApi = t != null && t.apiCount > 0 && onlineStatus.canCallApi()
        val hasAudio = t != null && t.audioCount > 0 && onlineStatus.canDownloadAudio()
        val hasPitchInfo = t != null && t.pitchInfoCount > 0 && onlineStatus.canDownloadAudio()
        val parts: MutableCollection<String?> = ArrayList()
        if (hasApi) {
            parts.add(String.format(Locale.ROOT, "%d background tasks", t!!.apiCount))
        }
        if (hasAudio) {
            parts.add(String.format(Locale.ROOT, "%d audio download tasks", t!!.audioCount))
        }
        if (hasPitchInfo) {
            parts.add(
                String.format(
                    Locale.ROOT,
                    "%d pitch info download tasks",
                    t!!.pitchInfoCount
                )
            )
        }
        if (parts.isEmpty()) {
            actionBar.subtitle = null
        } else {
            actionBar.subtitle = ObjectSupport.join(", ", "", "", parts) + "... ⌛"
        }
        val menu = menu
        if (menu != null) {
            val flushTasksItem = menu.findItem(R.id.action_flush_tasks)
            if (t != null && flushTasksItem != null) {
                flushTasksItem.isVisible = !t.isEmpty
            }
        }
    }

    private fun liveSessionStateOnChangeHelper() {
        val menu = menu
        val session = Session.getInstance()
        if (menu != null) {
            val sessionLogItem = menu.findItem(R.id.action_session_log)
            if (sessionLogItem != null) {
                sessionLogItem.isVisible = !session.isInactive
            }
            val abandonSessionItem = menu.findItem(R.id.action_abandon_session)
            if (abandonSessionItem != null) {
                abandonSessionItem.isVisible = session.canBeAbandoned()
            }
            val wrapupSessionItem = menu.findItem(R.id.action_wrapup_session)
            if (wrapupSessionItem != null) {
                wrapupSessionItem.isVisible = session.canBeWrappedUp()
            }
            val selfStudyItem = menu.findItem(R.id.action_self_study)
            if (selfStudyItem != null) {
                selfStudyItem.isVisible = session.isInactive
            }
        }
    }

    private fun onCreateBase() {
        creationTheme = ActiveTheme.getCurrentTheme()
        setContentView(layoutId)
        val toolbar = toolbar
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            val actionBar = supportActionBar
            if (actionBar != null) {
                if (!mainActivity) {
                    actionBar.setDisplayHomeAsUpEnabled(true)
                    actionBar.setDisplayShowHomeEnabled(true)
                }
                LiveTaskCounts.getInstance().observe(this) { t: TaskCounts? ->
                    ObjectSupport.safe {
                        onCreateBaseLiveTaskCounts(
                            actionBar,
                            t
                        )
                    }
                }
            }
        }
        LiveSessionState.getInstance()
            .observe(this) { t: SessionState? -> ObjectSupport.safe { liveSessionStateOnChangeHelper() } }
        LiveSessionProgress.getInstance().observe(this) { t: Any? ->
            ObjectSupport.safe {
                val menu = menu
                val session = Session.getInstance()
                if (menu != null) {
                    val wrapupSessionItem = menu.findItem(R.id.action_wrapup_session)
                    if (wrapupSessionItem != null) {
                        wrapupSessionItem.isVisible = session.canBeWrappedUp()
                    }
                }
            }
        }
        LiveLevelDuration.getInstance().observe(this) { t: LevelDuration ->
            ObjectSupport.safe {
                val menu = menu
                if (menu != null) {
                    val testItem = menu.findItem(R.id.action_test)
                    if (testItem != null) {
                        val username = t.username
                        testItem.isVisible =
                            !ObjectSupport.isEmpty(username) && username == Identification.AUTHOR_USERNAME
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ObjectSupport.safe {
            onCreateBase()
            onCreateLocal(savedInstanceState)
        }
    }

    override fun onResume() {
        ObjectSupport.safe {
            super.onResume()
            if (creationTheme != null && creationTheme !== ActiveTheme.getCurrentTheme()) {
                recreate()
                return@safe
            }
            if (GlobalSettings.Api.getSyncOnOpen() && (lastPause == 0L && lastResume == 0L
                        || lastResume < lastPause && System.currentTimeMillis() - lastPause > 3 * Constants.MINUTE)
            ) {
                JobRunnerService.schedule(AutoSyncNowJob::class.java, "")
            }
            lastResume = System.currentTimeMillis()
            PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this)
            if (!showWithoutApiKey()) {
                val apiKey = GlobalSettings.Api.getApiKey()
                if (ObjectSupport.isEmpty(apiKey)) {
                    goToActivity(NoApiKeyHelpActivity::class.java)
                }
            }
            JobRunnerService.schedule(ActivityResumedJob::class.java, javaClass.simpleName)
            tickTimer = Timer()
            tickTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    ObjectSupport.safe {
                        JobRunnerService.schedule(
                            TickJob::class.java, ""
                        )
                    }
                }
            }, Constants.MINUTE, Constants.MINUTE)
            volumeControlStream = AudioManager.STREAM_MUSIC
            onResumeLocal()
            enableInteraction()
        }
    }

    override fun onPause() {
        ObjectSupport.safe {
            super.onPause()
            lastPause = System.currentTimeMillis()
            if (tickTimer != null) {
                tickTimer!!.cancel()
                tickTimer = null
            }
            PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this)
            onPauseLocal()
        }
    }

    private fun onCreateOptionsMenuHelper(menu: Menu): Boolean {
        menuInflater.inflate(optionsMenuId, menu)
        val sessionLogItem = menu.findItem(R.id.action_session_log)
        if (sessionLogItem != null) {
            val session = Session.getInstance()
            sessionLogItem.isVisible = !session.isInactive
        }
        val abandonSessionItem = menu.findItem(R.id.action_abandon_session)
        if (abandonSessionItem != null) {
            val session = Session.getInstance()
            abandonSessionItem.isVisible = session.canBeAbandoned()
        }
        val wrapupSessionItem = menu.findItem(R.id.action_wrapup_session)
        if (wrapupSessionItem != null) {
            val session = Session.getInstance()
            wrapupSessionItem.isVisible = session.canBeWrappedUp()
        }
        val viewLastFinishedItem = menu.findItem(R.id.action_view_last_finished)
        if (viewLastFinishedItem != null) {
            val session = Session.getInstance()
            viewLastFinishedItem.isVisible = session.lastFinishedSubjectId != -1L
        }
        val backToPresentationItem = menu.findItem(R.id.action_back_to_presentation)
        if (backToPresentationItem != null) {
            val session = Session.getInstance()
            backToPresentationItem.isVisible =
                session.type == SessionType.LESSON && session.isActive
        }
        val studyMaterialsItem = menu.findItem(R.id.action_study_materials)
        if (studyMaterialsItem != null) {
            studyMaterialsItem.isVisible =
                currentSubject != null && currentSubject!!.type.canHaveStudyMaterials()
        }
        val selfStudyItem = menu.findItem(R.id.action_self_study)
        if (selfStudyItem != null) {
            val session = Session.getInstance()
            selfStudyItem.isVisible = session.isInactive
        }
        val flushTasksItem = menu.findItem(R.id.action_flush_tasks)
        if (flushTasksItem != null) {
            flushTasksItem.isVisible = !LiveTaskCounts.getInstance().get().isEmpty
        }
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.action_search)
        if (searchItem != null && searchManager != null) {
            val searchView = searchItem.actionView as SearchView?
            searchView!!.setSearchableInfo(
                searchManager.getSearchableInfo(
                    ComponentName(
                        this,
                        BrowseActivity::class.java
                    )
                )
            )
        }
        val testItem = menu.findItem(R.id.action_test)
        if (testItem != null) {
            val username = LiveLevelDuration.getInstance().get().username
            testItem.isVisible =
                !ObjectSupport.isEmpty(username) && username == Identification.AUTHOR_USERNAME
        }
        val muteItem = menu.findItem(R.id.action_mute)
        if (muteItem != null) {
            val muted = WkApplication.getDatabase().propertiesDao().isMuted
            val imageId = if (muted) R.drawable.ic_volume_off_24dp else R.drawable.ic_volume_up_24dp
            val title = if (muted) "Unmute" else "Mute"
            muteItem.icon = ContextCompat.getDrawable(this, imageId)
            muteItem.title = title
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return ObjectSupport.safe(true) { onCreateOptionsMenuHelper(menu) }
    }

    private fun onOptionsItemSelectedHelper(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.action_settings) {
            goToPreferencesActivity(null)
            return true
        }
        if (itemId == R.id.action_search) {
            startSearch(null, false, null, false)
            return true
        }
        if (itemId == R.id.action_mute) {
            val muted = !WkApplication.getDatabase().propertiesDao().isMuted
            WkApplication.getDatabase().propertiesDao().isMuted = muted
            val imageId = if (muted) R.drawable.ic_volume_off_24dp else R.drawable.ic_volume_up_24dp
            val title = if (muted) "Unmute" else "Mute"
            item.icon = ContextCompat.getDrawable(this, imageId)
            item.title = title
            return true
        }
        if (itemId == R.id.action_dashboard) {
            goToMainActivity()
            return true
        }
        if (itemId == R.id.action_download_audio) {
            goToActivity(DownloadAudioActivity::class.java)
            return true
        }
        if (itemId == R.id.action_browse) {
            goToActivity(BrowseActivity::class.java)
            return true
        }
        if (itemId == R.id.action_view_last_finished) {
            val subjectId = Session.getInstance().lastFinishedSubjectId
            if (subjectId != -1L) {
                goToSubjectInfo(subjectId, emptyList(), FragmentTransitionAnimation.RTL)
            }
            return true
        }
        if (itemId == R.id.action_back_to_presentation) {
            Session.getInstance().goBackToPresentation()
            if (this !is SessionActivity) {
                goToActivity(SessionActivity::class.java)
            }
            return true
        }
        if (itemId == R.id.action_session_log) {
            goToSessionLog()
            return true
        }
        if (itemId == R.id.action_abandon_session) {
            val session = Session.getInstance()
            if (GlobalSettings.UiConfirmations.getUiConfirmAbandonSession()) {
                val numActive = session.numActiveItems.toLong()
                val numPending = session.numPendingItems.toLong()
                val numReported = session.numReportedItems.toLong()
                val numStarted = session.numStartedItems.toLong()
                val numNotStarted = numActive - numStarted
                var message = "Are you sure you want to abandon this session? If you do:"
                if (numPending > 0) {
                    message += String.format(
                        Locale.ROOT,
                        "\n- %d finished items will not be reported",
                        numPending
                    )
                }
                if (numReported > 0) {
                    message += String.format(
                        Locale.ROOT,
                        "\n- %d already finished items will still be reported",
                        numReported
                    )
                }
                if (numStarted > 0) {
                    message += String.format(
                        Locale.ROOT,
                        "\n- %d partially quizzed items will not be reported",
                        numStarted
                    )
                }
                if (numNotStarted > 0) {
                    message += String.format(
                        Locale.ROOT,
                        "\n- %d unquizzed items will not be reported",
                        numNotStarted
                    )
                }
                AlertDialog.Builder(this)
                    .setTitle("Abandon session?")
                    .setMessage(message)
                    .setIcon(R.drawable.ic_baseline_warning_24px)
                    .setNegativeButton("No") { dialog: DialogInterface?, which: Int -> }
                    .setNeutralButton("Yes and don't ask again") { dialog: DialogInterface?, which: Int ->
                        ObjectSupport.safe {
                            session.finish()
                            Toast.makeText(this, "Session abandoned", Toast.LENGTH_SHORT).show()
                            GlobalSettings.UiConfirmations.setUiConfirmAbandonSession(false)
                        }
                    }
                    .setPositiveButton("Yes") { dialog: DialogInterface?, which: Int ->
                        ObjectSupport.safe {
                            session.finish()
                            Toast.makeText(this, "Session abandoned", Toast.LENGTH_SHORT).show()
                        }
                    }.create().show()
            } else {
                session.finish()
                Toast.makeText(this, "Session abandoned", Toast.LENGTH_SHORT).show()
            }
            return true
        }
        if (itemId == R.id.action_wrapup_session) {
            val session = Session.getInstance()
            if (GlobalSettings.UiConfirmations.getUiConfirmWrapupSession()) {
                val numActive = session.numActiveItems.toLong()
                val numStarted = session.numStartedItems.toLong()
                val numNotStarted = numActive - numStarted
                var message = "Are you sure you want to wrap up this session? If you do:"
                if (numStarted > 0) {
                    message += String.format(
                        Locale.ROOT,
                        "\n- %d partially quizzed items will remain in the session",
                        numStarted
                    )
                }
                if (numNotStarted > 0) {
                    message += String.format(
                        Locale.ROOT,
                        "\n- %d unquizzed items will be removed from the session",
                        numNotStarted
                    )
                }
                AlertDialog.Builder(this)
                    .setTitle("Wrap up session?")
                    .setMessage(message)
                    .setIcon(R.drawable.ic_baseline_warning_24px)
                    .setNegativeButton("No") { dialog: DialogInterface?, which: Int -> }
                    .setNeutralButton("Yes and don't ask again") { dialog: DialogInterface?, which: Int ->
                        ObjectSupport.safe {
                            session.wrapup()
                            Toast.makeText(this, "Session wrapping up...", Toast.LENGTH_SHORT)
                                .show()
                            GlobalSettings.UiConfirmations.setUiConfirmWrapupSession(false)
                        }
                    }
                    .setPositiveButton("Yes") { dialog: DialogInterface?, which: Int ->
                        ObjectSupport.safe {
                            session.wrapup()
                            Toast.makeText(this, "Session wrapping up...", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }.create().show()
            } else {
                session.wrapup()
                Toast.makeText(this, "Session wrapping up...", Toast.LENGTH_SHORT).show()
            }
            return true
        }
        if (itemId == R.id.action_study_materials) {
            val subject = currentSubject
            if (subject != null && subject.type.canHaveStudyMaterials()) {
                goToStudyMaterialsActivity(subject.id)
            }
            return true
        }
        if (itemId == R.id.action_self_study) {
            goToActivity(SelfStudyStartActivity::class.java)
            return true
        }
        if (itemId == R.id.action_sync_now) {
            JobRunnerService.schedule(SyncNowJob::class.java, "")
            return true
        }
        if (itemId == R.id.action_flush_tasks) {
            AlertDialog.Builder(this)
                .setTitle("Flush background tasks?")
                .setMessage(TextUtil.renderHtml(Constants.FLUSH_TASKS_WARNING))
                .setIcon(R.drawable.ic_baseline_warning_24px)
                .setNegativeButton("No") { dialog: DialogInterface?, which: Int -> }
                .setPositiveButton("Yes") { dialog: DialogInterface?, which: Int ->
                    ObjectSupport.safe {
                        JobRunnerService.schedule(FlushTasksJob::class.java, "")
                        Toast.makeText(this, "Background tasks flushed!", Toast.LENGTH_SHORT).show()
                    }
                }.create().show()
            return true
        }
        if (itemId == R.id.action_about) {
            goToActivity(AboutActivity::class.java)
            return true
        }
        if (itemId == R.id.action_support) {
            goToActivity(SupportActivity::class.java)
            return true
        }
        if (itemId == R.id.action_test) {
            Toast.makeText(this, "Test!", Toast.LENGTH_SHORT).show()
            goToActivity(TestActivity::class.java)
            return true
        }
        if (itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return ObjectSupport.safe(true) { onOptionsItemSelectedHelper(item) }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        ObjectSupport.safe {
            JobRunnerService.schedule(SettingChangedJob::class.java, key)
            if ("theme" == key) {
                recreate()
                WkApplication.getInstance().resetTheme()
            }
        }
    }

    /**
     * When changing content within an activity, collapse the search box.
     */
    protected fun collapseSearchBox() {
        ObjectSupport.safe {
            val menu = menu
            if (menu != null) {
                val searchItem = menu.findItem(R.id.action_search)
                searchItem?.collapseActionView()
            }
        }
    }

    /**
     * Get the current theme based on user settings.
     *
     * @return the theme
     */
    override fun getTheme(): Theme? {
        if (createdTheme == null) {
            ObjectSupport.safe {
                createdTheme = super.getTheme()
                if (createdTheme != null) {
                    createdTheme!!.applyStyle(ActiveTheme.getCurrentTheme().styleId, true)
                }
            }
        }
        return createdTheme
    }

    override fun getToolbar(): Toolbar? {
        return ObjectSupport.safeNullable { findViewById(R.id.toolbar) }
    }

    val menu: Menu?
        /**
         * Get the menu for this activity.
         *
         * @return the menu or null if it doesn't exist (yet).
         */
        get() = ObjectSupport.safeNullable {
            val toolbar = toolbar ?: return@safeNullable null
            toolbar.menu
        }

    override fun goToActivity(clas: Class<out AbstractActivity?>) {
        ObjectSupport.safe { startActivity(Intent(this, clas)) }
    }

    /**
     * Jump to the preferences activity immediately.
     *
     * @param rootKey the key of the screen to jump to, or null for the root screen
     */
    protected fun goToPreferencesActivity(rootKey: String?) {
        ObjectSupport.safe {
            val intent = Intent(this, PreferencesActivity::class.java)
            if (rootKey != null) {
                intent.putExtra("rootKey", rootKey)
            }
            startActivity(intent)
        }
    }

    override fun goToMainActivity() {
        ObjectSupport.safe {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }

    override fun goToSubjectInfo(
        id: Long,
        ids: List<Long>,
        animation: FragmentTransitionAnimation
    ) {
        ObjectSupport.safe {
            val a = LongArray(ids.size)
            for (i in a.indices) {
                a[i] = ids[i]
            }
            goToSubjectInfo(id, a, animation)
        }
    }

    override fun goToSubjectInfo(id: Long, ids: LongArray, animation: FragmentTransitionAnimation) {
        ObjectSupport.safe {
            if (this is BrowseActivity) {
                this.loadSubjectInfoFragment(id, ids, animation)
                return@safe
            }
            val intent = Intent(this, BrowseActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("ids", ids)
            startActivity(intent)
        }
    }

    /**
     * Go to the session log fragment.
     */
    private fun goToSessionLog() {
        ObjectSupport.safe {
            if (this is BrowseActivity) {
                this.loadSessionLogFragment()
                return@safe
            }
            val intent = Intent(this, BrowseActivity::class.java)
            intent.putExtra("sessionLog", true)
            startActivity(intent)
        }
    }

    override fun goToSearchResult(searchType: Int, searchParameters: String, presetName: String?) {
        ObjectSupport.safe {
            if (this is BrowseActivity) {
                this.loadSearchResultFragment(presetName, searchType, searchParameters)
                return@safe
            }
            val intent = Intent(this, BrowseActivity::class.java)
            intent.putExtra("searchType", searchType)
            intent.putExtra("searchParameters", searchParameters)
            if (presetName != null) {
                intent.putExtra("presetName", presetName)
            }
            startActivity(intent)
        }
    }

    /**
     * Go to the resurrect activity with the supplied list of subject IDs to resurrect.
     *
     * @param ids the subject IDs
     */
    fun goToResurrectActivity(ids: LongArray?) {
        ObjectSupport.safe {
            val intent = Intent(this, ResurrectActivity::class.java)
            intent.putExtra("ids", ids)
            startActivity(intent)
        }
    }

    /**
     * Go to the burn activity with the supplied list of subject IDs to burn.
     *
     * @param ids the subject IDs
     */
    fun goToBurnActivity(ids: LongArray?) {
        ObjectSupport.safe {
            val intent = Intent(this, BurnActivity::class.java)
            intent.putExtra("ids", ids)
            startActivity(intent)
        }
    }

    private fun goToStudyMaterialsActivity(id: Long) {
        ObjectSupport.safe {
            val intent = Intent(this, StudyMaterialsActivity::class.java)
            intent.putExtra("id", id)
            startActivity(intent)
        }
    }

    private val currentSubject: Subject?
        /**
         * Get the subject that this activity is currently dealing with,
         * or null if there is no specific subject.
         *
         * @return the subject
         */
        private get() {
            val fragment = currentFragment
            return fragment?.currentSubject
        }

    /**
     * Update the current subject ID for this activity. This provides the
     * context to enable the menu option for study materials.
     */
    fun updateCurrentSubject() {
        ObjectSupport.safe {
            val menu = menu
            if (menu != null) {
                val studyMaterialsItem = menu.findItem(R.id.action_study_materials)
                if (studyMaterialsItem != null) {
                    studyMaterialsItem.isVisible =
                        currentSubject != null && currentSubject!!.type.canHaveStudyMaterials()
                }
            }
        }
    }

    protected val currentFragment: AbstractFragment?
        /**
         * The current fragment attached to the default fragment container view.
         *
         * @return the fragment or null if not found
         */
        protected get() {
            val manager = supportFragmentManager
            val fragment = manager.findFragmentById(R.id.fragment)
            return if (fragment is AbstractFragment) {
                fragment
            } else null
        }

    /**
     * Enable interactivity on the current activity. This makes buttons clickable, etc.
     *
     *
     *
     * The idea is that if an interaction can take time to resolve, further
     * interaction is disabled until it has been resolved. Allowing parallel actions
     * sometimes causes problems.
     *
     */
    protected fun enableInteraction() {
        ObjectSupport.safe {
            enableInteractionLocal()
            interactionEnabled = true
        }
    }

    /**
     * Disable interactivity on the current activity. This makes buttons non-clickable, etc.
     *
     *
     *
     * The idea is that if an interaction can take time to resolve, further
     * interaction is disabled until it has been resolved. Allowing parallel actions
     * sometimes causes problems.
     *
     */
    protected fun disableInteraction() {
        ObjectSupport.safe {
            interactionEnabled = false
            disableInteractionLocal()
        }
    }

    /**
     * Translate DIPs to pixels.
     *
     * @param dp the dimension in DIPs
     * @return the corresponding number of pixels
     */
    protected fun dp2px(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    /**
     * Handle the onCreate event. This is called after the generic handling has been done.
     *
     * @param savedInstanceState the saved instance state from onCreate()
     */
    protected abstract fun onCreateLocal(savedInstanceState: Bundle?)

    /**
     * Handle the onResume event. This is called after the generic handling has been done.
     */
    protected abstract fun onResumeLocal()

    /**
     * Handle the onPause event. This is called after the generic handling has been done.
     */
    protected abstract fun onPauseLocal()

    /**
     * See enableInteraction(), this is the part that must be implemented by each subclass.
     */
    protected abstract fun enableInteractionLocal()

    /**
     * See disableInteraction(), this is the part that must be implemented by each subclass.
     */
    protected abstract fun disableInteractionLocal()

    /**
     * Can this activity be activated without entering an API key?
     *
     * @return False if showing this activity should bounde to the NoApiKeyHelpActivity.
     */
    protected abstract fun showWithoutApiKey(): Boolean

    companion object {
        private var lastResume: Long = 0
        private var lastPause: Long = 0

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }
}