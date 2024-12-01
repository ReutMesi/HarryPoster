package com.example.harryposter

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var scrollPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val termsCheckBox = findViewById<CheckBox>(R.id.termsCheckBox)
        val recyclerView = findViewById<RecyclerView>(R.id.horizontalRecyclerView)
        val dateBtn = findViewById<MaterialButton>(R.id.date_dialog_btn)
        val locationBtn = findViewById<MaterialButton>(R.id.buttonLocation)
        val childTicketsPicker = findViewById<NumberPicker>(R.id.childTicketsPicker)
        val adultTicketsPicker = findViewById<NumberPicker>(R.id.adultTicketsPicker)
        val totalPriceTextView = findViewById<TextView>(R.id.totalPriceTextView)
        val getTicketsButton = findViewById<Button>(R.id.getTicketsButton)

        // Initialize RecyclerView with images
        val images = listOf(
            R.drawable.poster, R.drawable.img1, R.drawable.img2,
            R.drawable.img3, R.drawable.img4, R.drawable.img5,
            R.drawable.img6, R.drawable.img7, R.drawable.img8, R.drawable.img9
        )
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = PictureAdapter(images)

        // Start auto-scroll for RecyclerView
        startAutoScroll(recyclerView, images.size)

        // Set up ticket pickers
        setupTicketPickers(childTicketsPicker, adultTicketsPicker, totalPriceTextView)

        // Handle terms checkbox
        termsCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Thank you for agreeing to the terms!", Toast.LENGTH_SHORT).show()
            }
        }

        // Date Picker
        setupDatePicker(dateBtn)

        // Location Selection Dialog
        setupLocationPicker(locationBtn)

        // Handle order submission
        getTicketsButton.setOnClickListener {
            handleOrderSubmission(
                childTicketsPicker, adultTicketsPicker, dateBtn.text.toString(),
                locationBtn.text.toString(), termsCheckBox
            )
        }

        // Apply fade-in animation for movie description
        val movieDescriptionTextView = findViewById<TextView>(R.id.TextViewDescribe)
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.textview_fade_in)
        movieDescriptionTextView.startAnimation(fadeInAnimation)

        // Handle edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun startAutoScroll(recyclerView: RecyclerView, itemCount: Int) {
        val scrollRunnable = object : Runnable {
            override fun run() {
                if (itemCount == 0) return
                scrollPosition = (scrollPosition + 1) % itemCount
                recyclerView.smoothScrollToPosition(scrollPosition)
                handler.postDelayed(this, 3000) // Scroll every 3 seconds
            }
        }
        handler.postDelayed(scrollRunnable, 3000)
    }

    private fun setupTicketPickers(
        childTicketsPicker: NumberPicker,
        adultTicketsPicker: NumberPicker,
        totalPriceTextView: TextView
    ) {
        childTicketsPicker.minValue = 0
        childTicketsPicker.maxValue = 10
        adultTicketsPicker.minValue = 0
        adultTicketsPicker.maxValue = 10

        childTicketsPicker.setOnValueChangedListener { _, _, _ ->
            calculateTotalPrice(childTicketsPicker, adultTicketsPicker, totalPriceTextView)
        }
        adultTicketsPicker.setOnValueChangedListener { _, _, _ ->
            calculateTotalPrice(childTicketsPicker, adultTicketsPicker, totalPriceTextView)
        }
    }

    private fun calculateTotalPrice(
        childTicketsPicker: NumberPicker,
        adultTicketsPicker: NumberPicker,
        totalPriceTextView: TextView
    ) {
        val childPrice = 10
        val adultPrice = 20
        val total = childTicketsPicker.value * childPrice + adultTicketsPicker.value * adultPrice
        totalPriceTextView.text = "Total: $$total"
    }

    private fun setupDatePicker(dateBtn: MaterialButton) {
        dateBtn.setOnClickListener {
            val calendar = Calendar.getInstance()
            val listener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                val selectedDate = "$dayOfMonth/${month + 1}/$year"
                dateBtn.text = selectedDate
            }
            DatePickerDialog(
                this, listener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupLocationPicker(locationBtn: MaterialButton) {
        val locations = arrayOf(
            "YES Planet Jerusalem", "Cinema City Tel Aviv", "Rav Hen Dizengoff",
            "Globus Max Be'er Sheva", "YES Planet Haifa"
        )
        locationBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Select Location")
                .setItems(locations) { _, which -> locationBtn.text = locations[which] }
                .show()
        }
    }

    private fun handleOrderSubmission(
        childTicketsPicker: NumberPicker,
        adultTicketsPicker: NumberPicker,
        selectedDate: String,
        selectedLocation: String,
        termsCheckBox: CheckBox
    ) {
        val userName = findViewById<TextInputEditText>(R.id.name).text.toString()
        val userPhone = findViewById<TextInputEditText>(R.id.editTextPhone).text.toString()

        // Validate user inputs
        when {
            selectedLocation == "Where?" -> showToast("Please select a location")
            selectedDate == "When?" -> showToast("Please select a date")
            !termsCheckBox.isChecked -> showToast("Please agree to the terms and conditions")
            !isValidPhoneNumber(userPhone) -> showToast("Please enter a valid 10-digit phone number")
            userName.isEmpty() -> showToast("Please enter your name")
            childTicketsPicker.value <= 0 && adultTicketsPicker.value <= 0 -> showToast("Please select at least one ticket")
            else -> {
                val totalPrice = calculateTotalPrice(childTicketsPicker.value, adultTicketsPicker.value)
                showOrderSummary(
                    userName, userPhone, selectedDate, selectedLocation,
                    childTicketsPicker.value, adultTicketsPicker.value, totalPrice
                )
            }
        }
    }

    private fun calculateTotalPrice(childTickets: Int, adultTickets: Int): Int {
        val childPrice = 10 // Price per child ticket
        val adultPrice = 20 // Price per adult ticket
        return (childTickets * childPrice) + (adultTickets * adultPrice)
    }


    private fun showOrderSummary(
        userName: String, userPhone: String, selectedDate: String, selectedLocation: String,
        childTickets: Int, adultTickets: Int, totalPrice: Int
    ) {
        val orderSummary = """
        Name: $userName
        Phone: $userPhone
        Date: $selectedDate
        Location: $selectedLocation
        Child Tickets: $childTickets
        Adult Tickets: $adultTickets
        Total: $$totalPrice
    """.trimIndent()

        // Show order summary in a dialog
        AlertDialog.Builder(this)
            .setTitle("Your Order Summary")
            .setMessage(orderSummary)
            .setPositiveButton("Confirm") { dialog, _ ->
                // Handle purchase confirmation
                val confirmationMessage = "Your order has been placed successfully!"
                Toast.makeText(this, confirmationMessage, Toast.LENGTH_SHORT).show()
                clearFields()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun clearFields() {
        findViewById<TextInputEditText>(R.id.name).text?.clear()
        findViewById<TextInputEditText>(R.id.editTextPhone).text?.clear()

        findViewById<NumberPicker>(R.id.childTicketsPicker).value = 0
        findViewById<NumberPicker>(R.id.adultTicketsPicker).value = 0

        findViewById<MaterialButton>(R.id.date_dialog_btn).text = "When?"
        findViewById<MaterialButton>(R.id.buttonLocation).text = "Where?"

        findViewById<CheckBox>(R.id.termsCheckBox).isChecked = false
    }


    private fun showOrderSummary(
        userName: String, userPhone: String, date: String, location: String,
        childTickets: Int, adultTickets: Int
    ) {
        val totalPrice = childTickets * 10 + adultTickets * 20
        val summary = """
            Name: $userName
            Phone: $userPhone
            Date: $date
            Location: $location
            Child Tickets: $childTickets
            Adult Tickets: $adultTickets
            Total: $$totalPrice
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Order Summary")
            .setMessage(summary)
            .setPositiveButton("Confirm") { _, _ -> showToast("Order confirmed!") }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun isValidPhoneNumber(phone: String) = phone.length == 10 && phone.all { it.isDigit() }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
