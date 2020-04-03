package com.example.twitterappdemo

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_login.*
import java.io.ByteArrayOutputStream
import java.io.FileDescriptor
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class Login : AppCompatActivity() {

    private var mAuth: FirebaseAuth?=null
    private var database= FirebaseDatabase.getInstance()
    private var myRef= database.reference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

            mAuth= FirebaseAuth.getInstance()
        ivimageperson.setOnClickListener(View.OnClickListener{
            checkPermission()

        })
    }


    fun loginTOFirebase(email:String, password:String){
        mAuth!!.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){task ->
                if (task.isSuccessful){
                    Toast.makeText(applicationContext,"successful login",Toast.LENGTH_LONG).show()
                    saveImageInFirebase()
                }else{
                    Toast.makeText(applicationContext,"fail login", Toast.LENGTH_LONG).show()
                }
            }

    }


    fun saveImageInFirebase() {

        var currentUser =mAuth!!.currentUser
        val email:String=currentUser!!.email.toString()
        val storage= FirebaseStorage.getInstance()
        val storageRef=storage.getReferenceFromUrl("gs://twitterappdemo-b474b.appspot.com/")
        val df=SimpleDateFormat("ddMMyyHHmmss")
        val dateObj=Date()
        val imagePath=splitString(email) + "." + df.format(dateObj)+".png"
        val imageRef=storageRef.child("images/$imagePath")
        //ivimageperson.isDrawingCacheEnabled=true
        //ivimageperson.buildDrawingCache()

        val drawable= ivimageperson.drawable as BitmapDrawable
        val bitmap=drawable.bitmap
        val baos=ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100,baos)
        val data=baos.toByteArray()
        val uploadTask=imageRef.putBytes(data)
        uploadTask.addOnFailureListener{
            Toast.makeText(applicationContext,"fail to upload", Toast.LENGTH_LONG).show()

        }.addOnSuccessListener { taskSnapshot ->

            var downloadURL=taskSnapshot.storage.downloadUrl!!.toString()!!

            myRef.child("users").child(currentUser.uid).child("email").setValue(currentUser.email)
            myRef.child("users").child(currentUser.uid).child("profileImage").setValue(downloadURL)

            loadTweets()
        }
    }

    fun splitString(email: String):String{
        val split=email.split("@")
        return split[0]
    }

    override fun onStart() {
        super.onStart()
        loadTweets()
    }

    fun loadTweets(){
        var currentUser= mAuth!!.currentUser

        if (currentUser!=null){
            var intent = Intent(this,MainActivity::class.java)
            intent.putExtra("email", currentUser.email)
            intent.putExtra("uid",currentUser.uid)
            startActivity(intent)
        }
    }

    val READIMAGE:Int=253
    fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED){

                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), READIMAGE)
                return
            }
        }
        loadImage()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when(requestCode){
            READIMAGE->{
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    loadImage()
                }else{
                    Toast.makeText(applicationContext,"Cannot access your images",Toast.LENGTH_LONG).show()
                }
            }
            else-> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
    val PICK_IMAGE_CODE=123
    fun loadImage(){

        var intent= Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(intent, PICK_IMAGE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_CODE && data!=null && resultCode ==  RESULT_OK){
            decodeUri(data.data as Uri)
        }
    }

    fun decodeUri(uri: Uri) {
        var parcelFD: ParcelFileDescriptor? = null
        try {
            parcelFD = contentResolver.openFileDescriptor(uri, "r")
            val imageSource: FileDescriptor = parcelFD!!.fileDescriptor

            // Decode image size
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            BitmapFactory.decodeFileDescriptor(imageSource, null, o)

            // the new size we want to scale to
            val requiredSize = 1024

            // Find the correct scale value. It should be the power of 2.
            var tempWidth = o.outWidth
            var tempHeight = o.outHeight
            var scale = 1
            while (tempWidth > requiredSize || tempHeight > requiredSize) {
                tempWidth /= 2
                tempHeight /= 2
                scale *= 2
            }

            // decode with inSampleSize
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            val bitmap = BitmapFactory.decodeFileDescriptor(imageSource, null, o2)
            ivimageperson.setImageBitmap(bitmap)
        } catch (e: FileNotFoundException) {
            // handle errors
            e.printStackTrace()
        } catch (e: IOException) {
            // handle errors
            e.printStackTrace()
        } finally {
            if (parcelFD != null) try {
                parcelFD.close()
            } catch (e: IOException) {
                // ignored
                e.printStackTrace()
            }
        }
    }

    fun buLogin(view: View){

        loginTOFirebase(etEmail.text.toString(), etEmail.text.toString())
    }
}
