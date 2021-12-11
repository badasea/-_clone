package com.example.howlstagram.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ContentInfoCompat
import androidx.fragment.app.Fragment
import com.example.howlstagram.LoginActivity
import com.example.howlstagram.MainActivity
import com.example.howlstagram.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.view.*
import android.Manifest;
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager

class UserFragment  : Fragment() {
    val PICK_PROFILE_FROM_ALBUM = 10

    var auth: FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    var uid: String? = null
    var currentUserUid: String? = null

    var fragmentView: View? = null
    var followListenerRegistration: ListenerRegistration? = null
    var followingListenerRegistration: ListenerRegistration? = null
    var imageprofileListenerRegistration: ListenerRegistration? = null
    var recyclerListenerRegistration: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = inflater.inflate(R.layout.fragment_user, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        currentUserUid = auth?.currentUser?.uid
        if (arguments != null) {

            uid = arguments!!.getString("destinationUid")
            if (uid != null && uid == currentUserUid) {
                fragmentView!!.account_btn_follow_signout.text = getString(R.string.signout)
                fragmentView?.account_btn_follow_signout?.setOnClickListener {
                    activity?.finish()
                    startActivity(Intent(activity, LoginActivity::class.java))
                    auth?.signOut()
                }
            } else {
                fragmentView!!.account_btn_follow_signout.text = getString(R.string.follow)
                var mainActivity = (activity as MainActivity)
                mainActivity.toolbar_title_image.visibility = View.GONE
                mainActivity.toolbar_btn_back.visibility = View.VISIBLE
                mainActivity.toolbar_username.visibility = View.VISIBLE

                mainActivity.toolbar_username.text = arguments!!.getString("userID")
                mainActivity.toolbar_btn_back.setOnClickListener {
                    mainActivity.bottom_navigation.selectedItemId = R.id.action_home
                }
                fragmentView?.account_btn_follow_signout?.setOnClickListener {
                    requestFollow()
                }
            }
        }
        fragmentView?.account_iv_profile?.setOnCapturedPointerListener {
            if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                var photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                activity!!.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
            }
        }
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(activity!!, 3)
        fragmentView?.account_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()

        return fragmentView
    }
}