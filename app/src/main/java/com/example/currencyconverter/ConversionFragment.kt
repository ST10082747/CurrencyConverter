package com.example.currencyconverter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Filter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.currencyconverter.databinding.FragmentConversionBinding
import kotlinx.coroutines.launch


class ConversionFragment : Fragment() {

    private var _binding: FragmentConversionBinding? = null
    private val binding get() = _binding!!
    private val commonCurrencies = listOf("USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "INR")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConversionBinding.inflate(inflater, container, false)
        return binding.root
    }

    @UnstableApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCurrencyDropdowns()
        setupConvertButton()
    }

    @UnstableApi
    private fun setupCurrencyDropdowns() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d("ConversionFragment", "Fetching currencies...")
                val response = CurrencyApiClient.getCurrencies()
                Log.d("ConversionFragment", "Currencies response: $response")
                if (response.status == "success") {
                    val allCurrencies = response.currencies
                    val commonCurrencyList = commonCurrencies.mapNotNull { code ->
                        allCurrencies[code]?.let { "$code - $it" }
                    }

                    val adapter = object : ArrayAdapter<String>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        commonCurrencyList
                    ) {
                        override fun getFilter(): Filter {
                            return object : Filter() {
                                override fun performFiltering(constraint: CharSequence?): FilterResults {
                                    val filterResults = FilterResults()
                                    if (constraint == null || constraint.isEmpty()) {
                                        filterResults.values = commonCurrencyList
                                        filterResults.count = commonCurrencyList.size
                                    } else {
                                        val filteredList = allCurrencies.filter { (code, name) ->
                                            code.contains(constraint, true) || name.contains(constraint, true)
                                        }.map { (code, name) -> "$code - $name" }
                                        filterResults.values = filteredList
                                        filterResults.count = filteredList.size
                                    }
                                    return filterResults
                                }

                                override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                                    clear()
                                    if (results.count > 0) {
                                        addAll(results.values as List<String>)
                                    }
                                    notifyDataSetChanged()
                                }
                            }
                        }
                    }

                    (binding.fromCurrencyLayout.editText as? AutoCompleteTextView)?.setAdapter(adapter)
                    (binding.toCurrencyLayout.editText as? AutoCompleteTextView)?.setAdapter(adapter)
                } else {
                    Log.e("ConversionFragment", "Failed to fetch currencies: ${response.status}")
                    Toast.makeText(context, "Failed to fetch currencies", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ConversionFragment", "Error fetching currencies", e)
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @UnstableApi
    private fun setupConvertButton() {
        binding.convertButton.setOnClickListener {
            val amount = binding.amountEditText.text.toString().toDoubleOrNull()
            val fromCurrency = (binding.fromCurrencyLayout.editText as? AutoCompleteTextView)?.text.toString().split(" - ").firstOrNull()
            val toCurrency = (binding.toCurrencyLayout.editText as? AutoCompleteTextView)?.text.toString().split(" - ").firstOrNull()

            if (amount == null || fromCurrency.isNullOrEmpty() || toCurrency.isNullOrEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    Log.d("ConversionFragment", "Converting currency...")
                    val response = CurrencyApiClient.convertCurrency(fromCurrency, toCurrency, amount)
                    Log.d("ConversionFragment", "Conversion response: $response")
                    if (response.status == "success") {
                        val rate = response.rates[toCurrency]?.rate_for_amount
                        binding.resultTextView.text = "%.2f %s = %s %s".format(amount, fromCurrency, rate, toCurrency)
                    } else {
                        Log.e("ConversionFragment", "Conversion failed: ${response.status}")
                        Toast.makeText(context, "Conversion failed", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("ConversionFragment", "Error converting currency", e)
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}