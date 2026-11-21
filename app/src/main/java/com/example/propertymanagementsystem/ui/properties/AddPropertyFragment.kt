package com.example.propertymanagementsystem.ui.properties

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.databinding.FragmentAddPropertyBinding
import com.example.propertymanagementsystem.ui.viewmodel.PropertyViewModel
import com.google.android.material.snackbar.Snackbar

class AddPropertyFragment : Fragment() {

    private var _binding: FragmentAddPropertyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PropertyViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPropertyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val propertyTypes = listOf("Apartment", "Bedsitter", "Single Room", "Commercial", "Townhouse")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, propertyTypes)
        binding.actvPropertyType.setAdapter(adapter)

        binding.btnSaveProperty.setOnClickListener {
            val name = binding.etPropertyName.text?.toString().orEmpty()
            val location = binding.etLocation.text?.toString().orEmpty()
            val type = binding.actvPropertyType.text?.toString().orEmpty()
            val rentInput = binding.etMonthlyRent.text?.toString().orEmpty()
            val phone = binding.etCaretakerPhone.text?.toString().orEmpty()
            val isOccupied = binding.switchOccupied.isChecked

            val result = viewModel.validateAndAddProperty(
                name = name,
                location = location,
                type = type,
                rentInput = rentInput,
                phone = phone,
                isOccupied = isOccupied
            )

            if (!result.first) {
                Snackbar.make(binding.root, result.second, Snackbar.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), result.second, Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_addPropertyFragment_to_viewPropertiesFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
