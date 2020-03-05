package cy.org.rise.obsai

import android.os.Bundle
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
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.ui.CustomAdapter
import cy.org.rise.obsai.utils.InjectorUtils
import kotlinx.android.synthetic.main.fragment_obstacle_list.*

class ObstacleListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

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

        recyclerView = recycler_view
        recyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = CustomAdapter()
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
        })

        fab.setOnClickListener {
            // Check whether the relevant permission are granted before launching camera fragment
            if (isAllGranted(CAMERA, ACCESS_FINE_LOCATION)) {
                // All permissions granted
                findNavController().navigate(R.id.action_obstacleListFragment_to_cameraFragment)

            } else {
                // Show permission rationales
                val permissionHandler = createDialogRationale(R.string.app_name) {
                    onPermission(CAMERA, R.string.permission_camera_rationale)
                    onPermission(ACCESS_FINE_LOCATION, R.string.permission_location_rationale)
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
                        obs_type = "Mock obstacle",
                        latitude = 35.169160,
                        longitude = 33.361459,
                        x = 0.0,
                        y = 0.0,
                        z = 0.0,
                        photo = "jkdhfak"
                    )
                )
                true
            }
            R.id.action_delete_all -> {
                viewModel.deleteAll()
                Toast.makeText(context, "Deleted everything", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}