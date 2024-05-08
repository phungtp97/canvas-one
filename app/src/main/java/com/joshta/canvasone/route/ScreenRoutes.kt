sealed class Screen(val route: String) {
    data object MainScreen : Screen("main_screen")
    data object GalleryScreen : Screen("gallery_screen")
}