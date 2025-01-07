package ads.mediastreet.ai.repositories

import ads.mediastreet.ai.app.ApiClient
import ads.mediastreet.ai.model.AccountRequest
import ads.mediastreet.ai.model.AccountResponse
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object AccountRepository {
    private const val TAG = "AccountRepository"

    fun checkAccountStatus(merchantId: String, callback: (String) -> Unit) {
        Log.d(TAG, "Making API call to check account status for merchant: $merchantId")
        ApiClient.getmInstance().api.getAccountStatus(AccountRequest(merchantId))
            .enqueue(object : Callback<AccountResponse> {
                override fun onResponse(
                    call: Call<AccountResponse>,
                    response: Response<AccountResponse>
                ) {
                    if (response.isSuccessful) {
                        val accountResponse = response.body()
                        Log.d(TAG, "Account status check successful: ${accountResponse?.status}")
                        callback(accountResponse?.status ?: "error")
                    } else {
                        Log.e(TAG, "Account status check failed: ${response.code()}")
                        callback("error")
                    }
                }

                override fun onFailure(call: Call<AccountResponse>, t: Throwable) {
                    Log.e(TAG, "Account status check failed", t)
                    callback("error")
                }
            })
    }
}
