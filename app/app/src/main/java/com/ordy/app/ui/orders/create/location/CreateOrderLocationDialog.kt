package com.ordy.app.ui.orders.create.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.ordy.app.R
import com.ordy.app.api.RepositoryViewModelFactory
import com.ordy.app.api.util.ErrorHandler
import com.ordy.app.api.util.QueryStatus
import com.ordy.app.databinding.DialogCreateOrderLocationBinding
import com.ordy.app.ui.orders.create.CreateOrderViewModel
import kotlinx.android.synthetic.main.dialog_create_order_location.view.*

class CreateOrderLocationDialog : DialogFragment() {

    private val viewModel: CreateOrderLocationViewModel by viewModels {
        RepositoryViewModelFactory(
            requireContext()
        )
    }

    private val activityViewModel: CreateOrderViewModel by activityViewModels()

    private lateinit var listAdapter: CreateOrderLocationListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        inflater.inflate(R.layout.dialog_create_order_location, container, false)

        // Create binding for the fragment.
        val binding = DialogCreateOrderLocationBinding.inflate(inflater, container, false)
        binding.handlers = CreateOrderLocationHandlers(this, viewModel)
        binding.viewModel = viewModel

        // Setup the toolbar
        val toolbar: Toolbar = binding.root.toolbar
        toolbar.title = "Choose location"
        toolbar.setNavigationOnClickListener { dismiss() }

        // Setup the list view
        val listView: ListView = binding.root.locations
        val listViewEmpty: LinearLayout = binding.root.locations_empty

        // Create the list view adapter
        listAdapter = CreateOrderLocationListAdapter(
            requireContext(),
            this,
            viewModel,
            activityViewModel,
            listView
        )

        listView.apply {
            adapter = listAdapter
            emptyView = listViewEmpty
        }

        val searchLoading = binding.root.locations_search_loading

        // Watch changes to the the "search value"
        viewModel.searchValueData.observe(viewLifecycleOwner, Observer {

            // Update the locations
            viewModel.updateLocations()
        })

        // Watch changes to the "locations"
        viewModel.getLocationsMLD().observe(viewLifecycleOwner, Observer {

            // Show a loading indicator in the searchbox.
            // Hide the list view while loading.
            when (it.status) {
                QueryStatus.LOADING -> {
                    searchLoading.visibility = View.VISIBLE
                    listView.emptyView = null
                }

                QueryStatus.SUCCESS -> {
                    searchLoading.visibility = View.INVISIBLE
                    listView.emptyView = listViewEmpty
                }

                QueryStatus.ERROR -> {
                    searchLoading.visibility = View.INVISIBLE

                    ErrorHandler.handle(it.error, view)
                }

                else -> {
                }
            }

            // Update the list adapter
            listAdapter.update()
        })

        return binding.root
    }
}
