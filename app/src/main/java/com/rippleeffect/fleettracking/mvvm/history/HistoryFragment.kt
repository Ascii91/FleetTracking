package com.rippleeffect.fleettracking.mvvm.history

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rippleeffect.fleettracking.databinding.FragmentHistoryBinding
import com.rippleeffect.fleettracking.model.LocationRecord
import com.rippleeffect.fleettracking.mvvm.base.BaseView
import com.rippleeffect.fleettracking.mvvm.base.ParentInteractor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment : Fragment(), BaseView<HistoryState.ViewState, HistoryState.ViewAction> {


    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HistoryAdapter
    private lateinit var parentInteractor: ParentInteractor


    companion object {
        fun newInstance() = HistoryFragment()
    }

    private val viewModel: HistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToViewModel()
        setUpRecyclerView()

        viewModel.loadData()
    }

    private fun setUpRecyclerView() {
        binding.rvHistoryItems.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        if (!::adapter.isInitialized) {
            adapter = HistoryAdapter()

        }
        binding.rvHistoryItems.adapter = adapter
    }

    override fun processState(state: HistoryState.ViewState) {

        when (state) {

            HistoryState.ViewState.Loading -> setLoadingState()
            HistoryState.ViewState.LoadingError -> {
            }
            is HistoryState.ViewState.DataLoaded -> showData(state.items)
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentInteractor = context as ParentInteractor
    }

    private fun showData(items: List<LocationRecord>) {
        hideLoading()

        adapter.setItems(items)
    }

    private fun hideLoading() {
        parentInteractor.hideLoading()
    }

    private fun setLoadingState() {
        parentInteractor.showLoading()

    }

    override fun processAction(action: HistoryState.ViewAction) {
        when (action) {
            HistoryState.ViewAction.CloseApp -> TODO()
        }
    }

    override fun subscribeToViewModel() {
        viewModel.viewState.observe(requireActivity(), ::processState)
        viewModel.viewAction.observe(requireActivity(), ::processAction)
    }

}