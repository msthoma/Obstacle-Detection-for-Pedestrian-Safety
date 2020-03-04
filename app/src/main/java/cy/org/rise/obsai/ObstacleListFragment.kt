package cy.org.rise.obsai

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cy.org.rise.obsai.ui.AboutActivity
import cy.org.rise.obsai.utils.InjectorUtils
import kotlinx.android.synthetic.main.fragment_obstacle_list.*
import java.io.File
import java.util.*

class ObstacleListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dataset: Array<String>

    private val viewModel: ObstacleViewModel by viewModels {
        InjectorUtils.provideObstacleViewModelFactory(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Set toolbar menu
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_obstacle_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = recycler_view
        recyclerView.layoutManager = LinearLayoutManager(context)
//        initDataset()

        val adapter = CustomAdapter2()
        recyclerView.adapter = adapter

        viewModel.insertObstacle(
            Obstacle(
                UUID.randomUUID().toString(),
                obstacle = "kjsdfak",
                latitude = 35.16989,
                longitude = 33.36116
            )
        )

        viewModel.obstacles.observe(viewLifecycleOwner, Observer { obstacles ->
            adapter.setObstacles(obstacles)
            adapter.notifyDataSetChanged()
            Log.d(TAG(), obstacles.size.toString())
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
            findNavController().navigate(R.id.cameraFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                findNavController().navigate(R.id.action_obstacleListFragment_to_settingsFragment)
//                viewModel.insertObstacle(
//                    Obstacle(
//                        obstacle = "kjsdfak",
//                        latitude = 35.16989,
//                        longitude = 33.36116
//                    )
//                )
                true
            }
            R.id.action_sign_in -> {
                findNavController().navigate(R.id.action_obstacleListFragment_to_accountFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initDataset() {
        val path = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
        dataset = File(path).list()
    }
}