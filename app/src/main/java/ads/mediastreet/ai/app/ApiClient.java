package ads.mediastreet.ai.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final Log log = LogFactory.getLog(ApiClient.class);
    private final String userAgent;
    private static String BASE_URL;
    private static ApiClient mInstance;
    private final Retrofit retrofit;

    private ApiClient() {
        userAgent = System.getProperty("http.agent");
        RetrofitInterceptor interceptor = new RetrofitInterceptor();
        interceptor.setUserAgent(userAgent);
//        if (BuildConfig.DEBUG) {
//            // development build
//            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//
//        } else {
//            // production build
//            interceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
//        }
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        BASE_URL = "https://mediastreetai.com";
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor)
                .connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
        retrofit = new Retrofit.Builder().
                baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }

    public static synchronized ApiClient getmInstance() {
        if (mInstance == null) {
            mInstance = new ApiClient();
        }
        return mInstance;
    }

    public static synchronized void resetInstance() {
        mInstance = null;
    }

    public Api getApi() {
        return retrofit.create(Api.class);
    }
}
