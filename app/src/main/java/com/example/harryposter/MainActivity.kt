package com.example.harryposter

import android.annotation.SuppressLint
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
        val totalPriceTextView = findViewById<TextView>(R.id.totalPriceTextViewNumber)
        val getTicketsButton = findViewById<Button>(R.id.getTicketsButton)
        val termsButton = findViewById<Button>(R.id.termsButton)


        val images = listOf(
            R.drawable.poster, R.drawable.img1, R.drawable.img2,
            R.drawable.img3, R.drawable.img4, R.drawable.img5,
            R.drawable.img6, R.drawable.img7, R.drawable.img8, R.drawable.img9
        )
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = PictureAdapter(images)

        startAutoScroll(recyclerView, images.size)

        setupTicketPickers(childTicketsPicker, adultTicketsPicker, totalPriceTextView)

        termsCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this,
                    getString(R.string.thank_you_for_agreeing_to_the_terms), Toast.LENGTH_SHORT).show()
            }
        }

        setupDatePicker(dateBtn)

        setupLocationPicker(locationBtn)

        getTicketsButton.setOnClickListener {
            handleOrderSubmission(
                childTicketsPicker, adultTicketsPicker, dateBtn.text.toString(),
                locationBtn.text.toString(), termsCheckBox
            )
        }

        termsButton.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dialog_terms_conditions, null)

            builder.setView(dialogView)
            val dialog = builder.show()

            dialogView.findViewById<Button>(R.id.acceptButton).setOnClickListener {
                termsCheckBox.setChecked(true)
                dialog.dismiss()
            }
            dialog.show()
        }

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

    @SuppressLint("SetTextI18n")
    private fun calculateTotalPrice(
        childTicketsPicker: NumberPicker,
        adultTicketsPicker: NumberPicker,
        totalPriceTextView: TextView
    ) {
        val childPrice = 35
        val adultPrice = 47
        val total = childTicketsPicker.value * childPrice + adultTicketsPicker.value * adultPrice
        totalPriceTextView.text = "$total₪"
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
            getString(R.string.yes_planet_jerusalem),
            getString(R.string.cinema_city_tel_aviv), getString(R.string.rav_hen_dizengoff),
            getString(R.string.globus_max_be_er_sheva), getString(R.string.yes_planet_haifa)
        )
        locationBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_location))
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

        when {
            selectedLocation == getString(R.string.where)-> showToast(getString(R.string.please_select_a_location))
            selectedDate == getString(R.string.`when`) -> showToast(getString(R.string.please_select_a_date))
            !termsCheckBox.isChecked -> showToast(getString(R.string.please_agree_to_the_terms_and_conditions))
            !isValidPhoneNumber(userPhone) -> showToast(getString(R.string.please_enter_a_valid_10_digit_phone_number))
            userName.isEmpty() -> showToast(getString(R.string.please_enter_your_name))
            childTicketsPicker.value <= 0 && adultTicketsPicker.value <= 0 -> showToast(getString(R.string.please_select_at_least_one_ticket))
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
        val childPrice = 35
        val adultPrice = 47
        return (childTickets * childPrice) + (adultTickets * adultPrice)
    }


    private fun showOrderSummary(
        userName: String, userPhone: String, selectedDate: String, selectedLocation: String,
        childTickets: Int, adultTickets: Int, totalPrice: Int
    ) {
        val orderSummary = """
            ${getString(R.string.name)}: $userName
            ${getString(R.string.phone)}: $userPhone
            ${getString(R.string.date)}: $selectedDate
            ${getString(R.string.location)}: $selectedLocation
            ${getString(R.string.child_tickets)}: $childTickets
            ${getString(R.string.adult_tickets)}: $adultTickets
            ${getString(R.string.total)}: ${getString(R.string.currency_symbol)}$totalPrice
        """.trimIndent()


        val dialogView = layoutInflater.inflate(R.layout.summary_layout, null)

        dialogView.findViewById<TextView>(R.id.summary_content).text = orderSummary

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)

        val dialog = builder.create()
        val animatedImage = dialogView.findViewById<ImageView>(R.id.animatedImage)
        val animation = AnimationUtils.loadAnimation(this, R.anim.move)
        animatedImage.startAnimation(animation)

        dialogView.findViewById<Button>(R.id.acceptButton).setOnClickListener {
            val confirmationMessage = getString(R.string.your_order_has_been_placed_successfully)
            Toast.makeText(this, confirmationMessage, Toast.LENGTH_SHORT).show()
            clearFields()
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun clearFields() {
        findViewById<TextInputEditText>(R.id.name).text?.clear()
        findViewById<TextInputEditText>(R.id.editTextPhone).text?.clear()

        findViewById<NumberPicker>(R.id.childTicketsPicker).value = 0
        findViewById<NumberPicker>(R.id.adultTicketsPicker).value = 0

        findViewById<MaterialButton>(R.id.date_dialog_btn).text = getString(R.string.`when`)
        findViewById<MaterialButton>(R.id.buttonLocation).text = getString(R.string.where)

        findViewById<CheckBox>(R.id.termsCheckBox).isChecked = false
    }


    private fun isValidPhoneNumber(phone: String) = phone.length == 10 && phone.all { it.isDigit() }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}
