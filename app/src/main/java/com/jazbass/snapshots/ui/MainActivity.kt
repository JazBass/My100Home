package com.jazbass.snapshots.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.jazbass.snapshots.utils.HomeAux
import com.jazbass.snapshots.ui.fragments.HomeFragment
import com.jazbass.snapshots.ui.fragments.ProfileFragment
import com.jazbass.snapshots.R
import com.jazbass.snapshots.databinding.ActivityMainBinding
import com.jazbass.snapshots.ui.fragments.AddFragment
import com.jazbass.snapshots.utils.MainAux

class MainActivity : AppCompatActivity(), MainAux {

    private lateinit var mBinding: ActivityMainBinding

    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener
    private var mFirebaseAuth: FirebaseAuth? = null
    private val RC_SIGN_IN = 21

    private lateinit var mActiveFragment: Fragment
    private lateinit var mFragmentManager: FragmentManager

    private val authResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == RESULT_OK){
            Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show()
        }else{
            if (IdpResponse.fromResultIntent(it.data)==null){
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setUpAuth()
        setUpBottomNav()
    }

    private fun setUpAuth() {
        mFirebaseAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener {
            if (it.currentUser == null) {
                authResult.launch(AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(
                        listOf(
                            AuthUI.IdpConfig.EmailBuilder().build(),
                            AuthUI.IdpConfig.GitHubBuilder().build(),
                            AuthUI.IdpConfig.GoogleBuilder().build()
                        )
                    ).build())
            }
        }
    }

    private fun setUpBottomNav() {
        mFragmentManager = supportFragmentManager

        val homeFragment = HomeFragment()
        val addFragment = AddFragment()
        val profileFragment = ProfileFragment()

        mActiveFragment = homeFragment

        mFragmentManager.beginTransaction()
            .add(R.id.host_fragment, profileFragment, ProfileFragment::class.java.name)
            .hide(profileFragment).commit()
        mFragmentManager.beginTransaction()
            .add(R.id.host_fragment, addFragment, AddFragment::class.java.name)
            .hide(addFragment).commit()
        mFragmentManager.beginTransaction()
            .add(R.id.host_fragment, homeFragment, HomeFragment::class.java.name).commit()

        mBinding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.action_home -> {
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(homeFragment)
                        .commit()
                    mActiveFragment = homeFragment
                    true
                }
                R.id.action_add -> {
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(addFragment)
                        .commit()
                    mActiveFragment = addFragment
                    true
                }
                R.id.action_profile -> {
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(profileFragment)
                        .commit()
                    mActiveFragment = profileFragment
                    true
                }
                else -> false
            }
        }
        mBinding.bottomNav.setOnItemReselectedListener {
            when(it.itemId){
                R.id.host_fragment -> (homeFragment as HomeAux).goToTop()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth?.addAuthStateListener(mAuthListener)
    }
    override fun onPause() {
        super.onPause()
        mFirebaseAuth?.removeAuthStateListener(mAuthListener)
    }

    override fun showMessage(resId: Int, duration: Int) {
        Snackbar.make(mBinding.root, resId, duration)
            .setAnchorView(mBinding.bottomNav)
            .show()
    }
}
