package com.example.propertymanagementsystem.ui.properties

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.databinding.FragmentViewPropertiesBinding
import com.example.propertymanagementsystem.ui.viewmodel.PropertyViewModel

class ViewPropertiesFragment : Fragment() {

    private var _binding: FragmentViewPropertiesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PropertyViewModel by activityViewModels()
    private var selectedProperty: Property? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewPropertiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.allProperties.observe(viewLifecycleOwner) { properties ->
            if (properties.isNullOrEmpty()) {
                val emptyAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    listOf("No registered properties")
                )
                emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerProperties.adapter = emptyAdapter
                selectedProperty = null
            } else {
                val displayNames = properties.map { "${it.propertyName} (${it.location})" }
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    displayNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerProperties.adapter = adapter

                binding.spinnerProperties.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (position in properties.indices) {
                            selectedProperty = properties[position]
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        selectedProperty = null
                    }
                }
            }
        }

        binding.btnCallCaretaker.setOnClickListener {
            val phone = selectedProperty?.caretakerPhone.orEmpty().trim()
            if (phone.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phone")
                }
                startActivity(intent)
            } else {
                Toast.makeText(
                    requireContext(),
                    "No property or caretaker phone number available.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
