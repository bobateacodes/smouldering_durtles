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

import android.os.Bundle
import androidx.compose.runtime.Composable
//import androidx.activity.viewModels
import androidx.compose.ui.platform.ComposeView
import com.smouldering_durtles.wk.GlobalSettings
import com.smouldering_durtles.wk.R
import com.smouldering_durtles.wk.activities.ui.theme.SmoulderingdurtlesTheme
import com.smouldering_durtles.wk.proxy.ViewProxy
import com.smouldering_durtles.wk.ui.OnboardingScreen
import com.smouldering_durtles.wk.util.ObjectSupport

/**
 * A simple activity only used as a helper to get the user to supply an API key.
 *
 *
 *
 * As long as no valid API key is present, other activities force this one to
 * be launched.
 *
 */
class NoApiKeyHelpActivity
/**
 * The constructor.
 */
    : AbstractActivity(R.layout.activity_no_api_key_help, R.menu.no_api_key_help_options_menu) {
        private val saveButton = ViewProxy()
        private val apiKey = ViewProxy()
    override fun onCreateLocal(savedInstanceState: Bundle?) {
        findViewById<ComposeView>(R.id.my_composable).setContent {
            SmoulderingdurtlesTheme {
                OnboardingScreen()
            }
        }
//        saveButton.setDelegate(this, R.id.saveButton)
//        apiKey.setDelegate(this, R.id.apiKey)
//        val document = ViewProxy(this, R.id.document)
//        document.setTextHtml(Constants.NO_API_KEY_HELP_DOCUMENT)
//        document.setLinkMovementMethod()
//        saveButton.setOnClickListener { v: View? -> saveApiKey() }
//        apiKey.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
//            ObjectSupport.safe(
//                false
//            ) {
//                if (event == null && actionId == EditorInfo.IME_ACTION_DONE) {
//                    saveApiKey()
//                    return@safe true
//                }
//                if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
//                    saveApiKey()
//                    return@safe true
//                }
//                false
//            }
//        }
    }

    override fun onResumeLocal() {
        //
    }

    override fun onPauseLocal() {
        //
    }

    override fun enableInteractionLocal() {
        //saveButton.enableInteraction()
    }

    override fun disableInteractionLocal() {
        //saveButton.disableInteraction()
    }

    override fun showWithoutApiKey(): Boolean {
        return true
    }

    /**
     * Handler for the save button. Save the API key that was entered.
     */
    @Composable
    private fun saveApiKey(apiKey: ViewProxy) {
        ObjectSupport.safe {
            if (!interactionEnabled) {
                return@safe
            }
            disableInteraction()
            GlobalSettings.Api.setApiKey(apiKey.text)
            goToMainActivity()
        }
    }
}