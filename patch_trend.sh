sed -i 's/fun TrendAnalysisScreen(/fun TrendAnalysisScreen(isDark: Boolean = false, /g' app/src/main/java/com/example/ui/TrendAnalysisScreen.kt

sed -i 's/TrendAnalysisScreen()/TrendAnalysisScreen(isDark = isDark)/g' app/src/main/java/com/example/ui/DashboardScreen.kt
