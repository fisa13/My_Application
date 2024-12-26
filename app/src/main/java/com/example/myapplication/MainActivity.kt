package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = "userList") {
                composable("userList") { UserListScreen(viewModel(), navController) }
                composable("userDetail/{userLogin}") { backStackEntry ->
                    UserDetailScreen(backStackEntry.arguments?.getString("userLogin") ?: "", viewModel())
                }
            }
        }
    }
}

@Composable
fun UserListScreen(viewModel: UserViewModel, navController: NavController) {
    val users by viewModel.users.observeAsState(emptyList())

    LazyColumn {
        items(users) { user ->
            Card(modifier = Modifier.padding(8.dp).clickable {
                navController.navigate("userDetail/${user.login}")
            }) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Image(
                        painter = rememberImagePainter(user.avatar_url),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp).clip(CircleShape)
                    )
                    Text(user.login, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
fun UserDetailScreen(userLogin: String, viewModel: UserViewModel) {
    // Здесь можно сделать дополнительный запрос, если необходимо
    Text("Details for $userLogin")
}