package yunwen.exhibition.login_payment

interface Destinations {
    val route: String
}

object HomeScreen: Destinations {
    override val route = "HomeScreen"
}

object PayPal: Destinations {
    override val route = "PayPal"
}