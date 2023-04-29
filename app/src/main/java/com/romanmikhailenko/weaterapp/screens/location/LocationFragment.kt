package com.romanmikhailenko.weaterapp.screens.location

import android.database.DatabaseUtils
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.romanmikhailenko.weaterapp.R
import com.romanmikhailenko.weaterapp.data.model.city.City
import com.romanmikhailenko.weaterapp.data.model.city.UpdateCity
import com.romanmikhailenko.weaterapp.databinding.FragmentLocationBinding
import com.romanmikhailenko.weaterapp.screens.location.adapters.SearchAdapter
import com.romanmikhailenko.weaterapp.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class LocationFragment : Fragment() {
    private var _binding: FragmentLocationBinding? = null
    private val mBinding get() = _binding!!

    private val viewModel by viewModels<LocationViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationBinding.inflate(layoutInflater, container, false)
        setupObservers()
        setUpUI()
        mBinding.svSearchCity.requestFocus()

        return mBinding.root
    }

    override fun onStart() {
        WindowCompat.getInsetsController(requireActivity().window, requireView())
            .show(WindowInsetsCompat.Type.ime())
        super.onStart()
    }


    private fun setupObservers() {
        viewModel.cityByQuery.observe(viewLifecycleOwner) {
            it?.let { resource ->
                with(mBinding) {
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data!!.isNotEmpty()) {
                                pbSearch.visibility = View.GONE
                                rvSearchedResult.visibility = View.VISIBLE
                                tvNoResult.visibility = View.GONE
                                setUpRecyclerView(it.data)
                            } else {
                                pbSearch.visibility = View.GONE
                                rvSearchedResult.visibility = View.GONE
                                tvNoResult.visibility = View.VISIBLE
                            }
                        }
                        Status.ERROR -> {
                            showFailedView(it.message)
                        }
                        Status.LOADING -> {

                        }
                    }
                }
            }
        }
    }

    private fun setUpRecyclerView(data: List<City>) {
        val searchAdapter = SearchAdapter(SearchAdapter.OnClickListener {
            Log.d("kavo", "pize")
            viewModel.updateSavedCities(UpdateCity(it.id, 1))

            findNavController().navigate(R.id.action_location_to_citiesFragment)
        })
        mBinding.rvSearchedResult.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = searchAdapter
        }
        searchAdapter.differ.submitList(data)


    }

    private fun setUpUI() {
        mBinding.svSearchCity.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val searchedQuery = if (query!!.contains("'")) DatabaseUtils.sqlEscapeString(query)
                    .replace("'", "") else query
                viewModel.getCityByQuery(searchedQuery)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val searchedQuery =
                    if (newText!!.contains("'")) DatabaseUtils.sqlEscapeString(newText)
                        .replace("'", "") else newText
                viewModel.getCityByQuery(searchedQuery)
                return false
            }

        })
    }

    private fun showFailedView(message: String?) {
        with(mBinding) {
            pbSearch.visibility = View.GONE
            rvSearchedResult.visibility = View.GONE
            tvNoResult.visibility = View.VISIBLE
            tvNoResult.text = message
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}