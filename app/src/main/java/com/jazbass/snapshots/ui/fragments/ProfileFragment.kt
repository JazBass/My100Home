package com.jazbass.snapshots.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.jazbass.snapshots.R
import com.jazbass.snapshots.databinding.FragmentProfileBinding
import com.jazbass.snapshots.utils.FragmentAux

class ProfileFragment : Fragment(), FragmentAux {

    private lateinit var mBinding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentProfileBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpButton()
        refresh()
    }

    private fun setUpButton() {
        mBinding.btnLogOut.setOnClickListener {
            context?.let {
                MaterialAlertDialogBuilder(it)
                    .setTitle(R.string.dialog_logout_title)
                    .setPositiveButton(R.string.dialog_delete_confirm) { _, _ -> signOut() }
                    .setNegativeButton(R.string.dialog_delete_cancel, null)
                    .show()
            }
        }
    }

    private fun signOut() {
        context?.let {
            AuthUI.getInstance().signOut(it)
                .addOnCompleteListener {
                    Toast.makeText(context, "See you", Toast.LENGTH_SHORT).show()
                    with(mBinding) {
                        tvName.text = ""
                        tvEmail.text = ""
                    }
                    (activity?.findViewById(R.id.bottom_nav) as? BottomNavigationView)?.selectedItemId =
                        R.id.action_home
                }
        }
    }

    override fun refresh() {
        with(mBinding) {
            tvName.text = FirebaseAuth.getInstance().currentUser?.displayName
            tvEmail.text = FirebaseAuth.getInstance().currentUser?.email
        }
    }
}
