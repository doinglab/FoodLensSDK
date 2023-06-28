package com.doinglab.sdk.example.foodlenstestapplication

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import com.doinglab.foodlens.sdk.*
import com.doinglab.foodlens.sdk.errors.BaseError
import com.doinglab.foodlens.sdk.network.model.NutritionResult
import com.doinglab.foodlens.sdk.network.model.RecognitionResult
import com.doinglab.foodlens.sdk.network.model.UserSelectedResult
import com.doinglab.sdk.example.foodlenstestapplication.listview.ListViewAdapter
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

    val REQ_PICTURE = 0x02

    var recognitionResult: RecognitionResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        uiService.setUiServiceMode(UIServiceMode.USER_SELECTED_WITH_CANDIDATES)

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
        startActivityForResult(intent, REQ_PICTURE)
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
                foodNutritionInfo += "$carbon $protein $fat"
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_PICTURE && resultCode == RESULT_OK && null != data) {
            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(
                selectedImage!!,
                filePathColumn, null, null, null
            )
            cursor!!.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()
            val byteData = readContentIntoByteArray(File(picturePath))

            ns.setNutritionRetrieveMode(NutritionRetrieveMode.TOP1_NUTRITION_ONLY)
            ns.setLanguageConfig(LanguageConfig.KO)
            ns.predictMultipleFood(byteData, object : RecognizeResultHandler {
                override fun onSuccess(result: RecognitionResult) {
                    recognitionResult = result
                    tv_title!!.text = "Result from Network Service"

                    setRecognitionResultData(result)
                }

                override fun onError(errorReason: BaseError) {
                    Log.e("FOODLENS_LOG", errorReason.message)
                }
            })
            ns.getNutritionInfo(20, object : NutritionResultHandler {
                override fun onSuccess(result: NutritionResult) {}
                override fun onError(errorReason: BaseError) {}
            })
        }
        uiService!!.onActivityResult(requestCode, resultCode, data)
    }

    private fun readContentIntoByteArray(file: File): ByteArray {
        var fileInputStream: FileInputStream? = null
        val bFile = ByteArray(file.length().toInt())
        try {
            //convert file into array of bytes
            fileInputStream = FileInputStream(file)
            fileInputStream.read(bFile)
            fileInputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bFile
    }
}