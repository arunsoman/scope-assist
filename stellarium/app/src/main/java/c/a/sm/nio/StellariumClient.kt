package c.a.sm.nio

import android.content.Context
import org.chromium.net.CronetEngine
import org.chromium.net.CronetException
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class StellariumClient() {
    lateinit var cronetEngine: CronetEngine
    lateinit var executor: Executor
    val TAG = "network"

    public fun init(context: Context){
        cronetEngine = CronetEngine.Builder(context).build()
        executor = Executors.newSingleThreadExecutor()

    }

    fun sendRequest(url: String, onSuccess: (s: String) -> Unit, onFailure: (s: String) -> Unit) {

        val requestBuilder = cronetEngine.newUrlRequestBuilder(
                url, object : UrlRequest.Callback() {

            var start: Long = 0
            var stop: Long = 0
            private val bytesReceived = ByteArrayOutputStream()
            private val receiveChannel = Channels.newChannel(bytesReceived)

            override public fun onRedirectReceived(
                    request: UrlRequest, info: UrlResponseInfo, newLocationUrl: String) {
                android.util.Log.i(TAG, "****** onRedirectReceived ******")
                request.followRedirect()
            }

            override public fun onResponseStarted(request: UrlRequest, info: UrlResponseInfo) {
                android.util.Log.i(TAG, "****** Response Started ******")
                android.util.Log.i(TAG, "*** Headers Are *** " + info.getAllHeaders())

                request.read(ByteBuffer.allocateDirect(32 * 1024))
            }

            override public fun onReadCompleted(request: UrlRequest, info: UrlResponseInfo, byteBuffer: ByteBuffer) {
                android.util.Log.i(TAG, "****** onReadCompleted ******" + byteBuffer)
                byteBuffer.flip()
                try {
                    receiveChannel.write(byteBuffer)
                } catch ( e: IOException) {
                    android.util.Log.i(TAG, "IOException during ByteBuffer read. Details: ", e)
                }
                byteBuffer.clear()
                request.read(byteBuffer)
            }

            override public fun onSucceeded(request: UrlRequest, info: UrlResponseInfo) {

                stop = System.nanoTime()

                android.util.Log.i(TAG,
                        "****** Cronet Request Completed, the latency is " + (stop - start))

                android.util.Log.i(TAG,
                        "****** Cronet Request Completed, status code is " + info.getHttpStatusCode()
                                + ", total received bytes is " + info.getReceivedByteCount())
                onSuccess(bytesReceived.toString())
            }


            override fun onFailed(request: UrlRequest?, info: UrlResponseInfo?, error: CronetException?) {
                android.util.Log.i(TAG, "****** onFailed, url:${url} error is: " + error!!.message)
                onFailure(error!!.message!!)
            }
        }, executor)
        requestBuilder.build().start()
    }
}