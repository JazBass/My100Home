package com.jazbass.snapshots

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.jazbass.snapshots.databinding.FragmentAddBinding

class AddFragment : Fragment() {

    private val RC_GELLERY = 18
    private val PATH_SNAPSHOT = "snapshot"

    private lateinit var mBinding: FragmentAddBinding
    private lateinit var mStorageReference: StorageReference
    private lateinit var mDatabaseReference: DatabaseReference

    private var mPhotoSelectedUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentAddBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mBinding.btnPost.setOnClickListener { postSnapshot() }
        mBinding.btnSelect.setOnClickListener { openGallery() }
        mStorageReference = FirebaseStorage.getInstance().reference
        mDatabaseReference = FirebaseDatabase.getInstance().reference.child(PATH_SNAPSHOT)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, RC_GELLERY)
    }

    private fun postSnapshot() {
        mBinding.progressBar.visibility = View.VISIBLE

        //Error trying to use global storage reference
        val storageReference = mStorageReference.child(PATH_SNAPSHOT).child("myPhoto")

        val key = mDatabaseReference.push().key

        if (mPhotoSelectedUri != null) {
            storageReference.putFile(mPhotoSelectedUri!!)
                .addOnProgressListener {
                    val progress = (100 * it.bytesTransferred / it.totalByteCount).toDouble()
                    mBinding.progressBar.progress = progress.toInt()
                    mBinding.tvMessage.text = "$progress%"
                }
                .addOnCompleteListener{
                    mBinding.progressBar.visibility = View.INVISIBLE
                }
                .addOnSuccessListener {
                    Snackbar.make(mBinding.root, "Photo published", Snackbar.LENGTH_SHORT).show()
                    it.storage.downloadUrl.addOnSuccessListener {
                        saveSnapfhot(key!!, it.toString(), mBinding.etTitle.text.toString().trim())
                        mBinding.tilTitle.visibility = View.GONE
                        mBinding.tvMessage.text = getString(R.string.post_message_title)
                    }
                }
                .addOnFailureListener{
                    Snackbar.make(mBinding.root, "ERROR, try again!", Snackbar.LENGTH_SHORT).show()
                }
        }

    }

    fun saveSnapfhot(key: String, url: String, title: String) {
        val snapshot = Snapshot(title= title, photoUrl = url)
        mDatabaseReference.child(key).setValue(snapshot)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == RC_GELLERY) {
            mPhotoSelectedUri = data?.data
            mBinding.imgPhoto.setImageURI(mPhotoSelectedUri)
            mBinding.tilTitle.visibility = View.VISIBLE
            mBinding.tvMessage.text = getString(R.string.post_message_valid_title)
        }
    }
}