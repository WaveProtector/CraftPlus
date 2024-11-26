//package com.example.craftplus
//
//import android.content.Intent
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.Surface
//import androidx.compose.ui.Modifier
//import androidx.activity.enableEdgeToEdge
//import com.example.craftplus.ui.theme.CraftPlusTheme
//import com.firebase.ui.auth.AuthUI
//import com.firebase.ui.database.FirebaseRecyclerOptions
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.database.DatabaseReference
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.ktx.database
//import com.google.firebase.ktx.Firebase
//import com.google.firebase.storage.StorageReference
//import com.google.firebase.storage.ktx.storage
//
//
//class FirebaseController {
//
//
//        // Firebase instance variables
//        private lateinit var auth: FirebaseAuth
//        private lateinit var db: FirebaseDatabase
//
//        override fun onCreate(savedInstanceState: Bundle?) {
//            //enableEdgeToEdge()
//            super.onCreate(savedInstanceState)
//            setContent {
//                CraftPlusTheme {
//                    Surface(
//                        modifier = Modifier.fillMaxSize(),
//                    ) {
//                        CraftPlusApp {  }()
//                    }
//                }
//            }
//
//            // Initialize Firebase Auth and check if the user is signed in
//            auth = Firebase.auth
//            if (auth.currentUser == null) {
//                // Not signed in, launch the Sign In activity
//                startActivity(Intent(this, SignInActivity::class.java))
//                finish()
//                return
//            }
//
//            // Initialize Realtime Database
//            db = Firebase.database
//        }
//
//        public override fun onStart() {
//            super.onStart()
//            //signOut()
//            // Check if user is signed in.
//            if (auth.currentUser == null) {
//                // Not signed in, launch the Sign In activity
//                startActivity(Intent(this, SignInActivity::class.java))
//                finish()
//                return
//            }
//        }
//
//
//        private fun signOut() {
//            AuthUI.getInstance().signOut(this)
//            startActivity(Intent(this, SignInActivity::class.java))
//            finish()
//        }
//
//    }
//
//}