package cy.org.rise.obsai

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cy.org.rise.obsai.ui.AboutActivity
import kotlinx.android.synthetic.main.fragment_obstacle_list.*
import java.io.File

class ObstacleListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dataset: Array<String>

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
        initDataset()

        // Show empty view message
        if (dataset.isEmpty()) {
            empty_list_view.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            empty_list_view.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }

        val adapter = CustomAdapter(dataset)
        recyclerView.adapter = adapter

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
                Toast.makeText(context, "Settings", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_sign_in -> {
                findNavController().navigate(R.id.action_obstacleListFragment_to_accountFragment)
                true
            }
            R.id.action_about -> {
                startActivity(Intent(activity, AboutActivity::class.java))
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