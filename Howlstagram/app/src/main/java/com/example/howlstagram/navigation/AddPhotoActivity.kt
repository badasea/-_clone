package com.example.howlstagram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.howlstagram.R
import com.example.howlstagram.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity: AppCompatActivity() {

    var photoUrl: Uri? = null
    val PICK_IMAGE_FROM_ALBUM = 0

    var storage: FirebaseStorage? = null
    var firestore: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_add_photo)

        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val photoPickerIntent = Intent(Intent.ACTION_PICK)

        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        addphoto_image.setOnClickListener {
            val photoPickerIntent = Intent(Intent(Intent.ACTION_PICK))
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
        }
        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if(resultCode == Activity.RESULT_OK) {
                println(data?.data)
                photoUrl = data?.data
                addphoto_image.setImageURI(data?.data)
            } else {
                finish()
            }
        }
    }

    fun contentUpload() {
        progress_bar.visibility = View.VISIBLE

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_.PNG"
        val storageRef = storage?.reference?.child("images")?.child(imageFileName)
        storageRef?.putFile(photoUrl!!)?.addOnSuccessListener { taskSnapshot ->
            progress_bar.visibility = View.GONE

            Toast.makeText(this, getString(R.string.upload_success),
            Toast.LENGTH_LONG).show()
            val uri = taskSnapshot.downloadUrl

            val contentDTO = ContentDTO()

            contentDTO.imageUrl = uri!!>toString()
            contentDTO.uid = auth?.currentUser?.uid
            contentDTO.explain = addphoto_edit_explain.text.toString()
            contentDTO.userId = auth?.currentUser?.email
            contentDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)

            setResult(Activity.RESULT_OK)
            finish()
        }
            ?.addOnFailureListener{
                progress_bar.visibility = View.GONE

                Toast.makeText(this, getString(R.string.upload_fail),Toast.LENGTH_LONG).show()
            }
    }
}