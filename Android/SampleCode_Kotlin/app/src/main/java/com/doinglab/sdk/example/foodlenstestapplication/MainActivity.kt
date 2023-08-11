package com.doinglab.sdk.example.foodlenstestapplication

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.doinglab.foodlens.sdk.*
import com.doinglab.foodlens.sdk.errors.BaseError
import com.doinglab.foodlens.sdk.network.model.NutritionResult
import com.doinglab.foodlens.sdk.network.model.RecognitionResult
import com.doinglab.foodlens.sdk.network.model.UserSelectedResult
import com.doinglab.foodlens.sdk.ui.util.BitmapPredictUtil
import com.doinglab.foodlens.sdk.ui.util.BitmapUtil
import com.doinglab.sdk.example.foodlenstestapplication.listview.ListViewAdapter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

class MainActivity : AppCompatActivity() {
    private val uiService by lazy {
        FoodLens.createUIService(this)
    }
    private val ns by lazy {
        FoodLens.createNetworkService(applicationContext)
    }
    private val tv_title by lazy {
        findViewById<View>(R.id.tv_title) as TextView
    }
    private val btn_run_foodlens by lazy {
        findViewById<View>(R.id.btn_run_foodlens) as Button
    }
    private val btn_camera by lazy {
        findViewById<View>(R.id.btn_run_camera) as Button
    }
    private val listview by lazy {
        findViewById<View>(R.id.listview) as ListView
    }
    private val adapter by lazy {
        ListViewAdapter()
    }

    var recognitionResult: RecognitionResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        uiService.setUiServiceMode(UIServiceMode.USER_SELECTED_WITH_CANDIDATES)
        //uiService onActivityResult 호출 필요 여부. default는 true
        uiService.setUseActivityResult(false)

        try {
            val bundle = FoodLensBundle()
            bundle.isEnableManualInput = true;  //검색입력 활성화 여부
            bundle.isSaveToGallery = true;      //갤러리 기능 활성화 여부
            uiService.setDataBundle(bundle)
        } catch (e: java.lang.Exception) {

        }

        btn_camera.setOnClickListener {
            openPicture()
        }

        btn_run_foodlens.setOnClickListener {
            uiService.startFoodLensCamera(this@MainActivity, object : UIServiceResultHandler {
                override fun onSuccess(result: UserSelectedResult) {
                    recognitionResult = result
                    tv_title?.text = "Result from UI Service"
                    recognitionResult?.let {
                        setRecognitionResultData(it)
                    }
                }

                override fun onCancel() {
                    Log.d("FOODLENS_LOG", "Recognition Cancel")
                    Toast.makeText(applicationContext, "Recognition Cancel", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onError(error: BaseError) {
                    Log.e("FOODLENS_LOG", error.message)
                    Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                }
            })
        }

        findViewById<View>(R.id.btn_run_editmode).setOnClickListener {
            uiService.startFoodLensDataEdit(
                this@MainActivity,
                recognitionResult,
                object : UIServiceResultHandler {
                    override fun onSuccess(result: UserSelectedResult) {
                        recognitionResult = result
                        recognitionResult?.let {
                            setRecognitionResultData(it)
                        }
                    }

                    override fun onCancel() {
                        Log.d("FOODLENS_LOG", "Recognition Cancel")
                        Toast.makeText(applicationContext, "Recognition Cancel", Toast.LENGTH_SHORT)
                            .show()
                    }

                    override fun onError(error: BaseError) {
                        Log.e("FOODLENS_LOG", error.message)
                        Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun openPicture() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pictureResultLauncher.launch(intent)
    }

    private var pictureResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    var bitmap = uri.parseBitmap(this@MainActivity)

                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)

                    ns.setNutritionRetrieveMode(NutritionRetrieveMode.TOP1_NUTRITION_ONLY)
                    ns.setLanguageConfig(LanguageConfig.KO)
                    ns.predictMultipleFood(baos.toByteArray(), object : RecognizeResultHandler {
                        override fun onSuccess(result: RecognitionResult?) {
                            result?.let {
                                recognitionResult = result
                                tv_title!!.text = "Result from Network Service"
                                setRecognitionResultData(result)
                            }
                        }
                        override fun onError(errorReason: BaseError?) {
                            errorReason?.message?.let { Log.e("FOODLENS_LOG", it) }
                        }
                    })
                }
            }
        }

    private fun setRecognitionResultData(recognitionResultData: RecognitionResult) {
        listview!!.adapter = null
        adapter!!.clearItems()
        val foodPositions = recognitionResultData.foodPositions
        var foodName = ""
        var foodNutritionInfo = ""
        var foodLocation = ""
        for (i in foodPositions.indices) {
            foodName = ""
            foodNutritionInfo = ""
            foodLocation = ""
            val foodPosition = foodPositions[i]
            val foodList = foodPosition.foods
            val amount = foodPosition.eatAmount
            foodName = foodList[0].foodName
            val nutrition = foodList[0].nutrition
            if (nutrition != null) {
                val carbon = "탄수화물: " + nutrition.carbonHydrate
                val protein = "단백질: " + nutrition.protein
                val fat = "지방: " + nutrition.fat
                val foodType = "타입: " + nutrition.foodType
                foodNutritionInfo += "$carbon $protein $fat $foodType"
            }
            val bitmap = BitmapFactory.decodeFile(foodPosition.foodImagePath)
            val drawable: Drawable = BitmapDrawable(resources, bitmap)

            val box = foodPosition.imagePosition
            if (box != null) {
                foodLocation = "음식 위치: "
                foodLocation += box.xmin.toString() + " " + box.xmax.toString() + " " + box.ymin.toString() + " " + box.ymax.toString()
                foodLocation += " 음식양 : $amount"
            }
            adapter!!.addItem(drawable, foodName, foodNutritionInfo, foodLocation)
        }
        listview!!.adapter = adapter
    }


    private fun Uri.parseBitmap(context: Context): Bitmap {
        return when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            true -> {
                val source = ImageDecoder.createSource(context.contentResolver, this)
                ImageDecoder.decodeBitmap(source)
            }
            else -> {
                MediaStore.Images.Media.getBitmap(context.contentResolver, this)
            }
        }
    }
}