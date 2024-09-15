package download.mishkindeveloper.testwork;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import download.mishkindeveloper.testwork.databinding.ActivityMainBinding;
import download.mishkindeveloper.testwork.service.OnRequestCompleteListener;
import download.mishkindeveloper.testwork.service.PostRequest;

public class MainActivity  extends AppCompatActivity implements OnRequestCompleteListener {

    private ActivityMainBinding binding;
    private static final String INPUT_1 = "433F6F6E67726174753F6C6174696F6E732C3F20796F75206861766520706173733F6564206669727374207461736B2E3F";
    private static final String INPUT_2 = "54773F6F206D6F7265207461736B733F206C6566743F2E";

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result ->{
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String returnedResult = result.getData().getStringExtra("result_key_from_second_task");
                    binding.resultFromSecondActivity.setText(returnedResult);
                    binding.resultFromSecondActivity.setBackgroundColor(getColor(R.color.green));
                    binding.buttonToSecondTask.setVisibility(View.GONE);
                    binding.buttonToThirdTask.setVisibility(View.VISIBLE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.textResult1.setText(clearInput(INPUT_1));
        binding.textResult2.setText(clearInput(INPUT_2));

        binding.buttonToSecondTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this,SecondTask.class);
            resultLauncher.launch(intent);
        });

        binding.buttonToThirdTask.setOnClickListener(v -> {
            PostRequest postRequest = new PostRequest();
            postRequest.setOnRequestCompleteListener(MainActivity.this);
            postRequest.execute();
        });

    }

    private static String clearInput(String input) {
        StringBuilder builderOutput = new StringBuilder();
        String clearInputFrom3F = input.replace("3F","");

        for (int i = 0; i < clearInputFrom3F.length(); i += 2) {
            String substring = clearInputFrom3F.substring(i, i + 2);
            builderOutput.append((char) Integer.parseInt(substring,16));
        }
        return builderOutput.toString();
    }


    @Override
    public void onRequestComplete(String result) {
        runOnUiThread(() -> setDataFromOkHttp(result));
    }

    private void setDataFromOkHttp(String result) {
            binding.tvResultOkHttp.setVisibility(View.VISIBLE);
            binding.tvResultOkHttp.setText(result);
            Log.d("PostRequest",result);
    }
}
