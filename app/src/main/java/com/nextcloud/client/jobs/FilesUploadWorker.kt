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

package com.nextcloud.client.jobs

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.nextcloud.client.account.User
import com.nextcloud.client.account.UserAccountManager
import com.nextcloud.client.device.PowerManagementService
import com.nextcloud.client.network.ConnectivityService
import com.owncloud.android.datamodel.FileDataStorageManager
import com.owncloud.android.datamodel.ThumbnailsCacheManager
import com.owncloud.android.datamodel.UploadsStorageManager
import com.owncloud.android.db.OCUpload
import com.owncloud.android.lib.common.OwnCloudAccount
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.operations.UploadFileOperation
import java.io.File

class FilesUploadWorker(
    val uploadsStorageManager: UploadsStorageManager,
    val connectivityService: ConnectivityService,
    val powerManagementService: PowerManagementService,
    val userAccountManager: UserAccountManager,
    val context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    override fun doWork(): Result {
        // get all pending uploads
        for (upload in uploadsStorageManager.currentAndPendingUploadsForCurrentAccount) {
            // create upload file operation
            val user = userAccountManager.getUser(upload.accountName)
            if (user.isPresent) {
                val uploadFileOperation = createUploadFileOperation(upload, user.get())

                upload(uploadFileOperation, user.get())
            } else {
                // TODO remove upload
            }
        }

        return Result.success()
    }

    /**
     * from @{link FileUploader#retryUploads()}
     */
    private fun createUploadFileOperation(upload: OCUpload, user: User): UploadFileOperation {
        return UploadFileOperation(
            uploadsStorageManager,
            connectivityService,
            powerManagementService,
            user,
            null,
            upload,
            upload.nameCollisionPolicy,
            upload.localAction,
            context,
            upload.isUseWifiOnly,
            upload.isWhileChargingOnly,
            true,
            FileDataStorageManager(user, context.contentResolver)
        )
    }

    private fun upload(uploadFileOperation: UploadFileOperation, user: User) {
        var uploadResult: RemoteOperationResult<Any?>? = null

        // TODO update notification
        // TODO notifyUploadStart()
        // TODO sendBroadcastUploadStarted(mCurrentUpload);

        try {
            val storageManager = uploadFileOperation.storageManager

            // always get client from client manager, to get fresh credentials in case of update
            val ocAccount = OwnCloudAccount(user.toPlatformAccount(), context)
            val uploadClient = OwnCloudClientManagerFactory.getDefaultSingleton().getClientFor(ocAccount, context)
            uploadResult = uploadFileOperation.execute(uploadClient)

            // generate new Thumbnail
            val task = ThumbnailsCacheManager.ThumbnailGenerationTask(storageManager, user)
            val file = File(uploadFileOperation.originalStoragePath)
            val remoteId: String = uploadFileOperation.file.remoteId
            task.execute(ThumbnailsCacheManager.ThumbnailGenerationTaskObject(file, remoteId))
        } catch (e: Exception) {
            Log_OC.e(TAG, "Error uploading", e)
            uploadResult = RemoteOperationResult<Any?>(e)
        } finally {
            // var removeResult: Pair<UploadFileOperation?, String?>
            // if (mCurrentUpload.wasRenamed()) {
            //     removeResult = mPendingUploads.removePayload(
            //         mCurrentAccount.name,
            //         mCurrentUpload.getOldFile().getRemotePath()
            //     )
            //     // TODO: grant that name is also updated for mCurrentUpload.getOCUploadId
            // } else {
            //     removeResult = mPendingUploads.removePayload(
            //         mCurrentAccount.name,
            //         mCurrentUpload.getDecryptedRemotePath()
            //     )
            // }
            uploadsStorageManager.updateDatabaseUploadResult(uploadResult, uploadFileOperation)

            /// TODO notify result
            // notifyUploadResult(mCurrentUpload, uploadResult)
            // sendBroadcastUploadFinished(mCurrentUpload, uploadResult, removeResult.second)
        }
    }

    fun uploadFile() {
    }

    companion object {
        val TAG: String = FilesUploadWorker::class.java.simpleName
    }
}
