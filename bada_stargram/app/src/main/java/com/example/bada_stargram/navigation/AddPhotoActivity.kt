package com.example.bada_stargram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import bolts.Task
import com.example.bada_stargram.R
import com.example.bada_stargram.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest
import kotlinx.android.synthetic.main.activity_add_photo.*


class AddPhotoActivity: AppCompatActivity() {

    var photoUrl: Uri? = null
    val PICK_IMAGE_FROM_ALBUM = 0

    var storage : FirebaseStorage? = null
    var firestore : FirebaseFirestore? = null
    var auth: FirebaseAuth? = null

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
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if(resultCode == Activity.RESULT_OK) {
                photoUrl = data?.data
                addphoto_image.setImageURI(photoUrl)
            } else {
                finish()
            }
        }
    }

    fun contentUpload() {
        progress_bar.visibility = View.VISIBLE

        var timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timeStamp + "_.png"
        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

//        storageRef?.putFile(photoUrl!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
//            return@continueWithTask storageRef.downloadUrl
//        }?.addOnSuccessListener { uri ->
//            val contentDTO = ContentDTO()
//            contentDTO.imageUrl = uri.toString()
//            contentDTO.uid = auth?.currentUser?.uid
//            contentDTO.explain = addphoto_edit_explain.text.toString()
//            contentDTO.userId = auth?.currentUser?.email
//            contentDTO.timestamp = System.currentTimeMillis()
//            firestore?.collection("images")?.document()?.set(contentDTO)
//            setResult(Activity.RESULT_OK)
//            finish()
//        }

        storageRef?.putFile(photoUrl!!)?.addOnSuccessListener {
//            Toast.makeText(this, getString(R.string.upload_success),
//            Toast.LENGTH_LONG).show()
            storageRef.downloadUrl.addOnSuccessListener { uri->
                val contentDTO = ContentDTO()
                contentDTO.imageUrl = uri.toString()
                contentDTO.uid = auth?.currentUser?.uid
                contentDTO.explain = addphoto_edit_explain.text.toString()
                contentDTO.userId = auth?.currentUser?.email
                contentDTO.timestamp = System.currentTimeMillis()
                firestore?.collection("images")?.document()?.set(contentDTO)
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
//            ?.addOnFailureListener{
//                progress_bar.visibility = View.GONE
//
//                Toast.makeText(this, getString(R.string.upload_fail),Toast.LENGTH_LONG).show()
//            }
    }
}