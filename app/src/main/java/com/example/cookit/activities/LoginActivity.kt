package com.example.cookit.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.cookit.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor aguarde")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.loginBtn.setOnClickListener {

            validateData()
        }

    }

    private var email = ""
    private var password = ""

    private fun validateData(){

        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Formato de email inválido...", Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()){
            Toast.makeText(this, "Introduza a palavra-pass...", Toast.LENGTH_SHORT).show()

        }
        else{
            loginUser()
        }

    }

    private fun loginUser(){

        progressDialog.setMessage("A fazer login...")
        progressDialog.show()


        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                checkUser()
            }
            .addOnFailureListener { e->

                progressDialog.dismiss()
                Toast.makeText(this, "O login falhou devido a ${e.message}", Toast.LENGTH_SHORT).show()

            }

    }

    private fun checkUser(){

        progressDialog.setMessage("A confirmar utilizador...")

        val firebaseUser = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialog.dismiss()

                    val userType = snapshot.child("userType").value
                    if (userType == "user"){

                        startActivity(Intent(this@LoginActivity, DashboardUserActivity::class.java))
                        finish()

                    }
                    else if (userType == "admin")

                        startActivity(Intent(this@LoginActivity, DashboardAdminActivity::class.java))
                        finish()
                }

                override fun onCancelled(error: DatabaseError) {

                }


            })

    }

}