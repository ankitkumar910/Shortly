
const apiUrl = import.meta.env.VITE_API_URL;
const config = {
    apiBaseUrl:     `${apiUrl}/api/v1/shorten`, // Replace with your actual API base URL
    apiKey: 'YOUR_API_KEY_HERE', // Replace with your actual API key if required
    defaultBackgroundImage: 'assets/bg5.jpg', // Default background image path
    
}

export {config}