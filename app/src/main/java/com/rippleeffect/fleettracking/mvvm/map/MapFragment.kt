package com.rippleeffect.fleettracking.mvvm.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.rippleeffect.fleettracking.R
import com.rippleeffect.fleettracking.databinding.FragmentMapBinding
import com.rippleeffect.fleettracking.model.MapLocationRecord
import com.rippleeffect.fleettracking.mvvm.base.BaseView
import com.rippleeffect.fleettracking.mvvm.base.ParentInteractor
import dagger.hilt.android.AndroidEntryPoint
import pl.tajchert.nammu.Nammu
import pl.tajchert.nammu.PermissionCallback


@AndroidEntryPoint
class MapFragment : Fragment(), BaseView<MapState.ViewState, MapState.ViewAction>,
    OnMapReadyCallback {


    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap
    private lateinit var adapter: DaysAdapter
    private lateinit var markerBitmapLarge: Bitmap
    private lateinit var markerBitmapSmall: Bitmap
    private lateinit var parentInteractor: ParentInteractor


    companion object {
        fun newInstance() = MapFragment()
    }

    private val viewModel: MapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Nammu.init(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)



        binding.ivBack.setOnClickListener {

            val position = getVisibleItemPosition()
            if (position >= 1) {
                binding.rvDays.smoothScrollToPosition(position - 1)
            }

        }
        binding.ivForward.setOnClickListener {

            val position = getVisibleItemPosition()
            if (position < adapter.itemCount - 1) {
                binding.rvDays.smoothScrollToPosition(position + 1)
            }

        }

        markerBitmapLarge = BitmapFactory.decodeResource(resources, R.drawable.ic_marker_large)
        markerBitmapSmall = BitmapFactory.decodeResource(resources, R.drawable.ic_marker_small)
        setUpRecyclerView()
        subscribeToViewModel()

        viewModel.loadDateData()

    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentInteractor = context as ParentInteractor
    }

    private fun setUpRecyclerView() {
        binding.rvDays.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        if (!::adapter.isInitialized) {
            adapter = DaysAdapter()
        }
        binding.rvDays.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    setDateButtonsState()
                }
            }
        })
        binding.rvDays.adapter = adapter
        val snapper: SnapHelper = PagerSnapHelper()
        snapper.attachToRecyclerView(binding.rvDays)
    }

    override fun processState(state: MapState.ViewState) {

        when (state) {

            MapState.ViewState.Loading -> showLoading()
            MapState.ViewState.LoadingError -> {
            }
            is MapState.ViewState.DataLoaded -> showData(state.items)
            is MapState.ViewState.DatesLoaded -> showDates(state.items)
        }


    }

    private fun showDates(items: List<Long>) {
        adapter.setItems(items)
        binding.rvDays.scrollToPosition(items.size - 1)
        Handler(Looper.getMainLooper()).postDelayed({
            setDateButtonsState()
        }, 200)

    }

    private fun showData(items: List<MapLocationRecord>) {
        hideLoading()

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                map
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        map.clear()
        val builder = LatLngBounds.Builder()
        val markerList = ArrayList<Marker>()

        var point1: LatLng? = null
        var point2: LatLng? = null
        var counter = 1
        items.forEach {
            val latLng = LatLng(it.latitude, it.longitude)

            markerList.add(
                map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(it.getFormatedDuration())
                        .flat(true)
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromBitmap(if (it.isSingleTimePoint()) markerBitmapSmall else markerBitmapLarge))


                )
            )

            // Instantiating CircleOptions to draw a circle around the marker
            if(viewModel.isFilteringDisabled()) {
                val circleOptions = CircleOptions()
                circleOptions.center(latLng)
                circleOptions.radius(20.0)
                circleOptions.strokeColor(Color.BLACK)
                circleOptions.fillColor(Color.MAGENTA)
                circleOptions.strokeWidth(2f)
                map.addCircle(circleOptions)
            }
//
//

            if (point1 == null) {
                point1 = latLng
            } else if (point2 == null) {
                point2 = latLng
            }

            if (point1 != null && point2 != null) {
                map.addPolyline(PolylineOptions().add(point1, point2))
                point1 = point2
                point2 = null
            }


            builder.include(latLng)
        }

        binding.seekBar.max = items.size - 1


        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                markerList[progress].showInfoWindow()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })
        binding.seekBar.progress = items.size - 1



        if (items.isNotEmpty()) {
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
        }
    }

    private fun hideLoading() {
        parentInteractor.hideLoading()
    }


    private fun setDateButtonsState() {
        val visibleItemPosition = getVisibleItemPosition()

        viewModel.loadLocationData(adapter.getItem(visibleItemPosition))

        when {
            adapter.itemCount <= 1 -> {
                disableButton(binding.ivBack)
                disableButton(binding.ivForward)

            }
            visibleItemPosition == 0 -> {
                disableButton(binding.ivBack)
                enableButton(binding.ivForward)


            }
            visibleItemPosition == adapter.itemCount - 1 -> {
                enableButton(binding.ivBack)
                disableButton(binding.ivForward)

            }
            else -> {
                enableButton(binding.ivBack)
                enableButton(binding.ivForward)
            }
        }


    }

    private fun showLoading() {
        parentInteractor.showLoading()
    }

    override fun processAction(action: MapState.ViewAction) {
        when (action) {
            MapState.ViewAction.CloseApp -> TODO()
        }
    }

    override fun subscribeToViewModel() {
        viewModel.viewState.observe(requireActivity(), ::processState)
        viewModel.viewAction.observe(requireActivity(), ::processAction)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        this.map = map
        map.uiSettings.isMapToolbarEnabled = true
        map.uiSettings.setAllGesturesEnabled(true)

        if (Nammu.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            map.isMyLocationEnabled = true
            return
        } else {
            Nammu.askForPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION, object : PermissionCallback {
                    override fun permissionGranted() {
                        map.isMyLocationEnabled = true
                    }

                    override fun permissionRefused() {
                        map.isMyLocationEnabled = false
                    }
                })

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    private fun disableButton(imageView: ImageView) {
        imageView.animate().alpha(0.3f).withEndAction { imageView.isEnabled = false }
    }

    private fun enableButton(imageView: ImageView) {
        imageView.animate().alpha(1f).withEndAction { imageView.isEnabled = true }
    }

    private fun getVisibleItemPosition(): Int {
        return (binding.rvDays.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
    }

}