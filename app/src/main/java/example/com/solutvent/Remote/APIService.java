package example.com.solutvent.Remote;


import example.com.solutvent.Model.DataMessage;
import example.com.solutvent.Model.MyResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAdsVlvJc:APA91bG1kQ2YkaoJuEKB03U5ZQTcEVQ6_ALXhzkn2CKJviyi68q1iM0_m47FpWudwXd3OKL8M0oBxG0qLRpsXUGQeB7M82UcLjwbGeTadEMU4p84w2cT4WwJlkLGUyabdaBXAmfplady"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body DataMessage body);

}
