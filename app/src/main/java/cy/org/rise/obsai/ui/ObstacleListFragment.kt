package cy.org.rise.obsai.ui

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.assent.Permission.ACCESS_FINE_LOCATION
import com.afollestad.assent.Permission.CAMERA
import com.afollestad.assent.isAllGranted
import com.afollestad.assent.rationale.createDialogRationale
import com.afollestad.assent.runWithPermissions
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.gson.Gson
import cy.org.rise.obsai.R
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.utils.InjectorUtils
import cy.org.rise.obsai.utils.SessionManager
import cy.org.rise.obsai.utils.TAG
import kotlinx.android.synthetic.main.fragment_obstacle_list.*

/**
 * Displays a list with current obstacles.
 */
@ExperimentalStdlibApi
class ObstacleListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomAdapter
    private lateinit var sessionManager: SessionManager

    private val viewModel: ObstacleViewModel by viewModels {
        InjectorUtils.provideObstacleViewModelFactory(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Set toolbar menu
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_obstacle_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        recyclerView = recycler_view
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = CustomAdapter()
        recyclerView.adapter = adapter

        viewModel.obstacles.observe(viewLifecycleOwner, Observer { obstacles ->

            // Observe and set list of obstacles in recycler view
            adapter.setObstacles(obstacles)

            // Show empty view message
            if (adapter.itemCount == 0) {
                empty_list_view.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                empty_list_view.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }

            // Invalidate menu, to hide "Delete all" in case of empty db (see onCreateOptionsMenu)
            activity?.invalidateOptionsMenu()
        })

        fab.setOnClickListener {
            // Check whether the relevant permission are granted before launching camera fragment
            if (isAllGranted(CAMERA, ACCESS_FINE_LOCATION)) {
                // All permissions granted
                initiateObstacleCollectionWorkflow()
            } else {
                // Show permission rationales
                val permissionHandler = createDialogRationale(
                    R.string.permission_rationale_dialog_title
                ) {
                    onPermission(
                        CAMERA,
                        R.string.permission_camera_rationale
                    )
                    onPermission(
                        ACCESS_FINE_LOCATION,
                        R.string.permission_location_rationale
                    )
                }

                // Ask for permissions, and then launch camera fragment
                runWithPermissions(
                    CAMERA,
                    ACCESS_FINE_LOCATION,
                    rationaleHandler = permissionHandler
                ) { result ->
                    if (result.isAllGranted(CAMERA, ACCESS_FINE_LOCATION)) {
                        // function below deals with the case where GPS is off
                        initiateObstacleCollectionWorkflow()
                    } else {
                        // deal with permission rejections here
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)

        // Show/hide the delete all option, depending on whether the db is empty or not
        if (adapter.itemCount == 0) {
            menu.findItem(R.id.action_delete_all).isVisible = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                findNavController().navigate(R.id.action_obstacleListFragment_to_settingsFragment)
                true
            }
            R.id.action_sign_in -> {
                findNavController().navigate(R.id.action_obstacleListFragment_to_accountFragment)
                true
            }
            R.id.action_add_mock_element -> {
                val mockObstacle = Obstacle(
                    obstacleType = "MockObstacle",
                    location = Obstacle.Location(
                        latitude = 35.169160,
                        longitude = 33.361459
                    ),
                    altitude = 31.4,
                    orientation = Obstacle.Orientation(
                        x = 1.0,
                        y = 2.0,
                        z = 3.0
                    ),
                    photoPath = "pathToPhoto",
                    typeProbabilitiesCNN = generateRandomMap()
                )
                Log.d(TAG(), Gson().toJson(mockObstacle))
                viewModel.insertObstacle(mockObstacle)
                true
            }
            R.id.action_delete_all -> {
                requireContext().let { context ->
                    MaterialDialog(context).show {
                        title(R.string.dialog_delete_all_title)
                        message(R.string.dialog_delete_all_msg)
                        icon(R.drawable.ic_warning_black_24dp)
                        positiveButton(R.string.dialog_delete_all_positive) {
                            viewModel.deleteAll()
                            Toast.makeText(context, "Deleted everything", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }
                        negativeButton(R.string.dialog_negative_button) { dismiss() }
                        lifecycleOwner(viewLifecycleOwner)
                    }
                }
                true
            }
//            R.id.action_test_server_connection -> {
//                // this is temporary, and should be moved to the view model anyway
//                CoroutineScope(Dispatchers.Main).launch {
//
//                    val lr = async(Dispatchers.IO) {
//                        FiwareOrionApi.create(FiwareOrionApi.LOGIN_BASE_URL).login(
//                            // the authorization header token for logging in is generated as
//                            // described here https://github.com/FIWARE/tutorials.Securing-Access#oauth2-grant-flows
//                            // the Credentials object below does the same as described in the link
//                            Credentials.basic(
//                                "tutorial-dckr-site-0000-xpresswebapp",
//                                "tutorial-dckr-site-0000-clientsecret"
//                            ),
//                            username = "alice-the-admin@test.com",
//                            password = "test",
//                            grant_type = "password"
//                        )
//                    }
//                    lr.await().also { response ->
//                        if (response.code() == 200) {
//                            response.body()?.accessToken?.let { sessionManager.saveAuthToken(it) }
//                        }
//                        Log.d(TAG(), "accessToken set to ${sessionManager.fetchAuthToken()}")
//                    }
//
//                    Toast.makeText(context, lr.await().code().toString(), Toast.LENGTH_LONG).show()
//                    Log.d(
//                        TAG(), lr.await().toString() + Credentials.basic(
//                            "wirecloud-dckr-site-0000-00000000000",
//                            "wirecloud-docker-000000-clientsecret"
//                        )
//                    )
//                }
//                true
//            }
//            R.id.action_test_server_obstacle_input -> {
//                viewModel.insertServerObstacle(mockObstacle.toRestObstacle())
//                true
//            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // makes sure GPS is on before allowing user to take photo
    private fun initiateObstacleCollectionWorkflow() {
        context?.let { context ->
            // check if GPS is on first
            if (isGPSEnabled(context)) {
                findNavController().navigate(
                    R.id.action_obstacleListFragment_to_cameraFragment
                )
            } else {
                Toast.makeText(context, "GPS is off", Toast.LENGTH_SHORT).show()
                // Turning on GPS can be done from within the app using the Settings Client, see
                // https://developer.android.com/training/location/change-location-settings
                // which should provide a better experience

                MaterialDialog(context).show {
                    title(text = "GPS is disabled on your device.")
                    message(text = "Enable it now?")
                    positiveButton(text = "Yes") {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                    negativeButton(text = "No") { dismiss() }
                    lifecycleOwner(viewLifecycleOwner)
                }
            }
        }
    }

    // checks if GPS is on
    private fun isGPSEnabled(context: Context): Boolean =
        (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(
            LocationManager.GPS_PROVIDER
        )

    private fun generateRandomMap(): Map<String, Float> {
        val stringArray = resources.getStringArray(R.array.obstacle_types_array)
        return buildMap {
            stringArray.forEach { obsType ->
                this[obsType] = 0.0f
            }
        }
    }
}