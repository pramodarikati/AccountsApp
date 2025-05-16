package com.example.accountsapp.repository

import com.example.accountsapp.data.local.User
import com.example.accountsapp.data.model.UserDao
import com.example.accountsapp.data.model.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userDao: UserDao

) {

    fun signInWithGoogle(idToken: String, callback: (User?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val userModel = User(user?.displayName, user?.email, user?.photoUrl.toString())

                    CoroutineScope(Dispatchers.IO).launch {
                        userDao.insertUser(
                            UserEntity(
                                name = userModel.name.toString(),
                                email = userModel.email.toString(),
                                profilePictureUrl = userModel.profilePictureUrl.toString()
                            )
                        )
                    }

                    callback(userModel)
                } else {
                    callback(null)
                }
            }
    }
}
