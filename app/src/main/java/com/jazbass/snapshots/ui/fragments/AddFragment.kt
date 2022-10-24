package com.jazbass.snapshots.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.jazbass.snapshots.R
import com.jazbass.snapshots.SnapshotsApplication
import com.jazbass.snapshots.entities.Snapshot
import com.jazbass.snapshots.databinding.FragmentAddBinding
import com.jazbass.snapshots.utils.MainAux

class AddFragment : Fragment() {

    private lateinit var mBinding: FragmentAddBinding
    private lateinit var mStorageReference: StorageReference
    private lateinit var mDatabaseReference: DatabaseReference

    private var mainAux: MainAux? = null

    private var mPhotoSelectedUri: Uri? = null

    private val galleryResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                mPhotoSelectedUri = it.data?.data
                with(mBinding) {
                    imgPhoto.setImageURI(mPhotoSelectedUri)
                    tilTitle.visibility = View.VISIBLE
                    tvMessage.text = getString(R.string.post_message_valid_title)
                }
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentAddBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpButtons()
        setUpFirebase()
    }

    private fun setUpButtons() {
        with(mBinding) {
            btnPost.setOnClickListener { postSnapshot() }
            btnSelect.setOnClickListener { openGallery() }
        }
    }

    private fun setUpFirebase() {
        mStorageReference = FirebaseStorage.getInstance()
            .reference.child(SnapshotsApplication.PATH_SNAPSHOTS)
        mDatabaseReference = FirebaseDatabase.getInstance(SnapshotsApplication.EU_WEST_INSTANCE)
            .reference.child(SnapshotsApplication.PATH_SNAPSHOTS)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResult.launch(intent)
    }

    private fun postSnapshot() {
        if (mPhotoSelectedUri != null) {
            enableUI(false)
            mBinding.progressBar.visibility = View.VISIBLE

            val key = mDatabaseReference.push().key!!
            //Error trying to use global storage reference
            val storageReference =
                mStorageReference.child(FirebaseAuth.getInstance().currentUser!!.uid).child(key)

            storageReference.putFile(mPhotoSelectedUri!!)
                .addOnProgressListener {
                    val progress = (100 * it.bytesTransferred / it.totalByteCount).toDouble()
                    with(mBinding) {
                        progressBar.progress = progress.toInt()
                        tvMessage.text = String.format("%s%%", progress)
                    }
                }
                .addOnCompleteListener {
                    mBinding.progressBar.visibility = View.INVISIBLE
                }
                .addOnSuccessListener {
                    it.storage.downloadUrl.addOnSuccessListener { itUri ->
                        saveSnapshot(key, itUri.toString(), mBinding.etTitle.text.toString().trim())
                        mBinding.tilTitle.visibility = View.GONE
                        mBinding.tvMessage.text = getString(R.string.post_message_title)
                    }
                }
                .addOnFailureListener {
                    mainAux?.showMessage(R.string.post_message_fail_image_post)
                }
        }

    }

    private fun saveSnapshot(key: String, url: String, title: String) {
        val snapshot = Snapshot(title = title, photoUrl = url)
        mDatabaseReference.child(key).setValue(snapshot)
            .addOnSuccessListener {
                hideKeyboard()
                mainAux?.showMessage(R.string.post_message_post_success)

                with(mBinding) {
                    tilTitle.visibility = View.GONE
                    etTitle.setText("")
                    tilTitle.error = null
                    tvMessage.text = getString(R.string.post_message_title)
                    imgPhoto.setImageDrawable(null)
                }
            }
            .addOnCompleteListener { enableUI(true) }
            .addOnFailureListener {
                mainAux?.showMessage(R.string.post_message_fail_image_post)
            }
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)

    }

    private fun enableUI(enable: Boolean) {
        with(mBinding) {
            btnSelect.isEnabled = enable
            btnPost.isEnabled = enable
            tilTitle.isEnabled = enable
        }
    }

}