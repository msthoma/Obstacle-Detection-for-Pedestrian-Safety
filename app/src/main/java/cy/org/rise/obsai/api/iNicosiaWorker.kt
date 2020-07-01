package cy.org.rise.obsai.api

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class iNicosiaWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        TODO("Not yet implemented")
    }
}