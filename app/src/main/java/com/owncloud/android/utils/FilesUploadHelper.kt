/*
 *
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2022 Tobias Kaminsky
 * Copyright (C) 2022 Nextcloud GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.owncloud.android.utils

import android.content.Context
import androidx.work.WorkManager
import com.nextcloud.client.core.ClockImpl
import com.nextcloud.client.jobs.BackgroundJobManager
import com.nextcloud.client.jobs.BackgroundJobManagerImpl
import com.nextcloud.common.User
import com.owncloud.android.datamodel.OCFile
import com.owncloud.android.db.OCUpload
import com.owncloud.android.files.services.NameCollisionPolicy
import com.owncloud.android.lib.common.utils.Log_OC

class FilesUploadHelper(val context: Context) {
    val backgroundJobManager: BackgroundJobManager =
        BackgroundJobManagerImpl(WorkManager.getInstance(context), ClockImpl())

    fun uploadNewFile(
        user: com.nextcloud.client.account.User,
        localPaths: Array<String>,
        remotePaths: Array<String>,
        mimeTypes: Array<String>?,
        behaviour: Int,
        createRemoteFolder: Boolean,
        createdBy: Int,
        requiresWifi: Boolean,
        requiresCharging: Boolean,
        nameCollisionPolicy: NameCollisionPolicy
    ) {
        Log_OC.d(this, "upload new file")
    }

    companion object {
        fun uploadUpdatedFile(
            user: User,
            existingFiles: Array<OCFile>,
            behaviour: Int,
            nameCollisionPolicy: NameCollisionPolicy,
            disableRetries: Boolean
        ) {
            Log_OC.d(this, "upload updated file")
        }

        fun retryUpload(user: User, upload: OCUpload) {
        }

        fun uploadNewFile(
            user: com.nextcloud.client.account.User,
            localPaths: Array<String>,
            remotePaths: Array<String>,
            mimeTypes: Array<String>?,
            behaviour: Int,
            createRemoteFolder: Boolean,
            createdBy: Int,
            requiresWifi: Boolean,
            requiresCharging: Boolean,
            nameCollisionPolicy: NameCollisionPolicy
        ) {
            Log_OC.d(this, "upload new file")
        }
    }
}
