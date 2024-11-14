package yunwen.exhibition.login_payment

interface Destinations {
    val route: String
}


object Login: Destinations {
    override val route = "Login"
}


object Register: Destinations {
    override val route = "Register"
}

object HomeScreen: Destinations {
    override val route = "HomeScreen"
}

object PayPal: Destinations {
    override val route = "PayPal"
}