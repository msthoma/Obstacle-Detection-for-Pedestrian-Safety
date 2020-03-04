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
            findNavController().navigate(R.id.action_obstacleListFragment_to_cameraFragment)
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
                        obstacle = "kjsdfak",
                        obs_type = "crack",
                        latitude = 35.16989,
                        longitude = 33.36116,
                        x = 0.1,
                        y = 0.1,
                        z = 0.1,
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