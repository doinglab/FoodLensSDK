package com.doinglab.sdk.example.foodlenstestapplication;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.doinglab.foodlens.sdk.FoodLens;
import com.doinglab.foodlens.sdk.NetworkService;

import com.doinglab.foodlens.sdk.network.model.RecognitionResult;
import com.doinglab.foodlens.sdk.UIService;
import com.doinglab.foodlens.sdk.network.model.UserSelectedResult;
import com.doinglab.foodlens.sdk.errors.BaseError;
import com.doinglab.foodlens.sdk.network.model.Box;
import com.doinglab.foodlens.sdk.network.model.Food;
import com.doinglab.foodlens.sdk.network.model.FoodPosition;
import com.doinglab.foodlens.sdk.network.model.Nutrition;
import com.doinglab.foodlens.sdk.network.model.NutritionResult;
import com.doinglab.foodlens.sdk.NutritionResultHandler;
import com.doinglab.foodlens.sdk.RecognizeResultHandler;

import com.doinglab.foodlens.sdk.UIServiceResultHandler;
import com.doinglab.foodlens.sdk.UIServiceMode;
import com.doinglab.sdk.example.foodlenstestapplication.listview.ListViewAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import static com.doinglab.foodlens.sdk.NutritionRetrieveMode.TOP1_NUTRITION_ONLY;

public class MainActivity extends AppCompatActivity {

    UIService uiService;
    NetworkService ns;

    TextView tv_title;
    Button btn_run_foodlens, btn_camera;
    ListView listview ;
    ListViewAdapter adapter;
    final int REQ_PICTURE = 0x02;

    RecognitionResult recognitionResult = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uiService = FoodLens.createUIService(getApplicationContext());
        uiService.setUiServiceMode(UIServiceMode.USER_SELECTED_WITH_CANDIDATES);

        tv_title = (TextView)findViewById(R.id.tv_title);
        adapter = new ListViewAdapter() ;
        listview = (ListView) findViewById(R.id.listview);

        btn_camera = (Button)findViewById(R.id.btn_run_camera);
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPicture();

            }
        });

        btn_run_foodlens = (Button)findViewById(R.id.btn_run_foodlens);
        btn_run_foodlens.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uiService.startFoodLensCamera(MainActivity.this, new UIServiceResultHandler() {
                    @Override
                    public void onSuccess(UserSelectedResult result) {
                        recognitionResult = result;
                        tv_title.setText("Result from UI Service");
                        setRecognitionResultData(recognitionResult);
                    }

                    @Override
                    public void onCancel() {
                        Log.d("FOODLENS_LOG", "Recognition Cancel");
                        Toast.makeText(getApplicationContext(), "Recognition Cancel", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(BaseError error) {

                        Log.e("FOODLENS_LOG", error.getMessage());
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });

        findViewById(R.id.btn_run_editmode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uiService.startFoodLensDataEdit(MainActivity.this, recognitionResult, new UIServiceResultHandler() {
                    @Override
                    public void onSuccess(UserSelectedResult result) {
                        recognitionResult = result;
                        setRecognitionResultData(recognitionResult);
                    }

                    @Override
                    public void onCancel() {
                        Log.d("FOODLENS_LOG", "Recognition Cancel");
                        Toast.makeText(getApplicationContext(), "Recognition Cancel", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(BaseError error) {
                        Log.e("FOODLENS_LOG", error.getMessage());
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void openPicture() {

        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(intent, REQ_PICTURE);
    }

    private void setRecognitionResultData(UserSelectedResult recognitionResultData)
    {
        listview.setAdapter(null);
        adapter.clearItems();

        List<FoodPosition> foodPositions = recognitionResultData.getFoodPositions();

        String foodName = "";
        String foodNutritionInfo = "";
        String foodLocation = "";
        for(int i=0; i<foodPositions.size(); i++)
        {

            foodName = "";
            foodNutritionInfo = "";
            foodLocation = "";

            FoodPosition foodPosition = foodPositions.get(i);


            foodName = foodPosition.getUserSelectedFood().getFoodName();
            Nutrition nutrition = foodPosition.getUserSelectedFood().getNutrition();
            if(nutrition != null)
            {
                String carbon = "탄수화물: " + nutrition.getCarbonHydrate();
                String protein = "단백질: " + nutrition.getProtein();
                String fat = "지방: " + nutrition.getFat();

                foodNutritionInfo += (carbon +" " + protein +" "+fat);
            }

            Bitmap bitmap = BitmapFactory.decodeFile(foodPosition.getFoodImagePath());
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);


            Box box = foodPosition.getImagePosition();
            if(box!=null)
            {
                foodLocation = "음식 위치: ";
                foodLocation += String.valueOf(box.getXmin()) + " " + String.valueOf(box.getXmax()) + " " + String.valueOf(box.getYmin()) + " " + String.valueOf(box.getYmax());
            }

            adapter.addItem(drawable, foodName, foodNutritionInfo, foodLocation);
        }

        listview.setAdapter(adapter);
    }

    private void setRecognitionResultData(RecognitionResult recognitionResultData)
    {
        listview.setAdapter(null);
        adapter.clearItems();

        List<FoodPosition> foodPositions = recognitionResultData.getFoodPositions();

        String foodName = "";
        String foodNutritionInfo = "";
        String foodLocation = "";
        for(int i=0; i<foodPositions.size(); i++)
        {

            foodName = "";
            foodNutritionInfo = "";
            foodLocation = "";

            FoodPosition foodPosition = foodPositions.get(i);
            List<Food> foodList = foodPosition.getFoods();

            foodName = foodList.get(0).getFoodName();
            Nutrition nutrition = foodList.get(0).getNutrition();
            if(nutrition != null)
            {
                String carbon = "탄수화물: " + nutrition.getCarbonHydrate();
                String protein = "단백질: " + nutrition.getProtein();
                String fat = "지방: " + nutrition.getFat();

                foodNutritionInfo += (carbon +" " + protein +" "+fat);
            }

            Bitmap bitmap = BitmapFactory.decodeFile(foodPosition.getFoodImagePath());
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);


            Box box = foodPosition.getImagePosition();
            if(box!=null)
            {
                foodLocation = "음식 위치: ";
                foodLocation += String.valueOf(box.getXmin()) + " " + String.valueOf(box.getXmax()) + " " + String.valueOf(box.getYmin()) + " " + String.valueOf(box.getYmax());
            }

            adapter.addItem(drawable, foodName, foodNutritionInfo, foodLocation);
        }

        listview.setAdapter(adapter);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_PICTURE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            final byte[] byteData = readContentIntoByteArray(new File(picturePath));

            ns = FoodLens.createNetworkService(getApplicationContext());
            ns.setNutritionRetrieveMode(TOP1_NUTRITION_ONLY);
            ns.predictMultipleFood(byteData, new RecognizeResultHandler() {
                @Override
                public void onSuccess(RecognitionResult result) {

                    tv_title.setText("Result from Network Service");
                    setRecognitionResultData(result);

                }

                @Override
                public void onError(BaseError errorReason) {
                    Log.e("FOODLENS_LOG", errorReason.getMessage());

                }
            });

            ns.getNutritionInfo(20, new NutritionResultHandler() {
                @Override
                public void onSuccess(NutritionResult result) {

                }

                @Override
                public void onError(BaseError errorReason) {

                }
            });
        }

        uiService.onActivityResult(requestCode, resultCode, data);
    }

    private byte[] readContentIntoByteArray(File file) {
        FileInputStream fileInputStream = null;
        byte[] bFile = new byte[(int) file.length()];
        try {
            //convert file into array of bytes
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bFile;
    }


}
