package cy.org.rise.obsai.ui

import android.os.Bundle
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
import cy.org.rise.obsai.R
import cy.org.rise.obsai.api.FiwareOrionApi
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.utils.InjectorUtils
import cy.org.rise.obsai.utils.SessionManager
import cy.org.rise.obsai.utils.TAG
import kotlinx.android.synthetic.main.fragment_obstacle_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.Credentials

/**
 * Displays a list with current obstacles. *
 */
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

        sessionManager = SessionManager(context!!)

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
                findNavController().navigate(R.id.action_obstacleListFragment_to_cameraFragment)

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
                        findNavController().navigate(
                            R.id.action_obstacleListFragment_to_cameraFragment
                        )
                    } else {
                        // TODO deal with permission rejections
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
                viewModel.insertObstacle(
                    Obstacle(
                        obstacleType = "Mock obstacle",
                        location = Obstacle.Location(
                            latitude = 35.169160,
                            longitude = 33.361459
                        ),
                        orientation = Obstacle.Orientation(
                            x = 0.0,
                            y = 0.0,
                            z = 0.0
                        ),
                        photoPath = "jkdhfak"
                    )
                )
                true
            }
            R.id.action_delete_all -> {
                MaterialDialog(context!!).show {
                    title(R.string.dialog_delete_all_title)
                    message(R.string.dialog_delete_all_msg)
                    icon(R.drawable.ic_warning_black_24dp)
                    positiveButton(R.string.dialog_delete_all_positive) {
                        viewModel.deleteAll()
                        Toast.makeText(context, "Deleted everything", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                    negativeButton(R.string.dialog_delete_all_negative) {
                        dismiss()
                    }
                }
                true
            }
            R.id.action_test_server_connection -> {
                // TODO move this to view model
                CoroutineScope(Dispatchers.Main).launch {

                    val lr = async(Dispatchers.IO) {
                        FiwareOrionApi.create(FiwareOrionApi.LOGIN_BASE_URL).login(
                            // the authorization header token for logging in is generated as
                            // described here https://github.com/FIWARE/tutorials.Securing-Access#oauth2-grant-flows
                            // the Credentials object below does the same as described in the link
                            Credentials.basic(
                                "tutorial-dckr-site-0000-xpresswebapp",
                                "tutorial-dckr-site-0000-clientsecret"
                            ),
                            username = "alice-the-admin@test.com",
                            password = "test",
                            grant_type = "password"
                        )
                    }
                    lr.await().also { response ->
                        if (response.code() == 200) {
                            response.body()?.accessToken?.let { sessionManager.saveAuthToken(it) }
                        }
                        Log.d(TAG(), "accessToken set to ${sessionManager.fetchAuthToken()}")
                    }

                    Toast.makeText(context, lr.await().code().toString(), Toast.LENGTH_LONG).show()
                    Log.d(
                        TAG(), lr.await().toString() + Credentials.basic(
                            "wirecloud-dckr-site-0000-00000000000",
                            "wirecloud-docker-000000-clientsecret"
                        )
                    )
//                    lr.await().r
//                    val orionVersion = FiwareOrionApi.create().getOrionVersion()
//
//                    Log.d(TAG(), orionVersion.toString())
                }
                true
            }
            R.id.action_test_server_obstacle_input -> {
                viewModel.insertServerObstacle(
                    Obstacle(
                        obstacleType = "Mock obstacle",
                        location = Obstacle.Location(
                            latitude = 35.169160,
                            longitude = 33.361459
                        ),
                        orientation = Obstacle.Orientation(
                            x = 0.0,
                            y = 0.0,
                            z = 0.0
                        ),
                        photoPath = "jkdhfak"
                    ).toRestObstacle()
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}