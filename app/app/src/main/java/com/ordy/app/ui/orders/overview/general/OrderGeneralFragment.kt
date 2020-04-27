package com.ordy.app.ui.orders.overview.general

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.ordy.app.R
import com.ordy.app.databinding.FragmentOrderGeneralBinding
import com.ordy.app.ui.orders.overview.OverviewOrderViewModel
import kotlinx.android.synthetic.main.fragment_order_general.view.*

class OrderGeneralFragment : Fragment() {

    private val viewModel: OverviewOrderViewModel by activityViewModels()

    private lateinit var listAdapter: OrderGeneralListAdapter

    /**
     * Called when view is created.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_order_general, container, false)

        // Create binding for the fragment.
        val binding = FragmentOrderGeneralBinding.inflate(inflater, container, false)
        binding.handlers = OrderGeneralHandlers(this, viewModel)

        // Create the list view adapter
        listAdapter = OrderGeneralListAdapter(requireContext(), viewModel)
        binding.root.order_items.apply {
            adapter = listAdapter
            emptyView = binding.root.order_items_empty
        }

        // Update the list adapter when the "order" query updates
        viewModel.getOrderMLD().observe(viewLifecycleOwner, Observer {

            // Update the list view
            listAdapter.update()
        })

        return binding.root
    }
}