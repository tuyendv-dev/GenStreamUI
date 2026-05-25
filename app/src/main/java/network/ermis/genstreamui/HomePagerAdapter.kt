package network.ermis.genstreamui

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class HomePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MineFragment()
            1 -> DiscoveryFragment()
            2 -> FindGameFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
}
