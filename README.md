# NextChapter

Welcome to NextChapter! Swap books with readers around you and never worry about buying new books again!

## Group Members:
- Aviral Bal ([aviralbal](https://github.com/aviralbal))
- Kevin Shine George ([kevinsgeo](https://github.com/kevinsgeo))
- Saaketh Yerramsetti ([Saaketh-Y](https://github.com/Saaketh-Y))
- Tanya Santhosh ([TanyaCommits](https://github.com/TanyaCommits))

## Introduction
Books are expensive, and many avid readers end up with piles of books they no longer need but hesitate to sell. Recognizing the urgent need for more sustainable reading practices, NextChapter provides an eco-friendly alternative that maximizes the lifecycle of books by providing a dedicated platform that brings book lovers together, especially on a local level.

### Why is this important?
A report by [Book Riot](https://bookriot.com/book-price-increase/) indicates that the average cost of books has increased by 11-30%, limiting access to reading materials. Additionally, the production of new books significantly impacts the environment, contributing to deforestation and increased carbon emissions. NextChapter promotes a sustainable model of book consumption by extending the lifecycle of books through swapping.

### What is Our App?
NextChapter is a mobile-first Android app that allows users to swap books based on their preferences and location. Users can upload pictures of books they wish to swap, browse books offered by others, and arrange swaps with people nearby using geolocation. Users can also maintain a wishlist of books and receive notifications when a book from their list is uploaded nearby. If a swap is not found, users can choose to donate the books to local charity organizations.

## Tech Stack
### **Frontend:**
- **Jetpack Compose:** Used for building the app's modern, responsive, and declarative UI.
- **CameraX:** Integrated for capturing and scanning book barcodes directly from the app.
- **Material3 Design:** Ensures consistency and adherence to modern UI/UX principles.

### **Backend:**
- **Firebase Realtime Database:** Manages user profiles, book listings, reviews, wishlist notifications, and swapping history. Real-time synchronization ensures instant updates across devices.
- **Stream API:** Powers the chat functionality, enabling users to communicate in real-time to coordinate swaps.
- **Google Books API:** Fetches detailed book information (title, author, genre, thumbnail) for enhanced listings.
- **Google Maps API:** Provides geolocation services to facilitate local book swaps.

### **Third-Party Services:**
- **Google Books API:** Allows users to search for books by ISBN, author, or genre, expanding the database of available books.
- **Google Maps API:** Ensures accurate geolocation matching, helping users find swaps near them.

## Features
### Mobile-Specific Innovations:
1. **CameraX Integration:** Allows users to quickly upload clear images of books, making listings more visually appealing, by seamlessly scanning the ISBN from the book barcode.
2. **Geolocation Matching:** Uses Google Maps API to match users with others nearby for convenient swapping.
4. **Stream API Chat:** Seamless in-app messaging for real-time communication between users.

### Backend Highlights:
1. **Real-Time Database Management:** Firebase ensures fast and reliable storage of book listings, user profiles, and swapping history.
2. **Geolocation Services:** Matches users based on proximity for localized book swaps.
3. **Scalable Design:** Designed to handle large user bases with efficient query mechanisms and serverless Firebase backend.

## Target Audience:
- **Students:** Simplifies textbook swapping, reducing costs for college and school students.
- **Avid Readers:** Encourages trading fiction/non-fiction books instead of buying new ones.
- **Eco-Conscious Users:** Appeals to those committed to reducing their carbon footprint.
- **Local Communities:** Fosters resource sharing within neighborhoods.

## Testing and Deployment
### **Testing Environments:**
- Tested across Android versions to ensure compatibility and seamless functionality.

### **User Testing:**
- Involves collecting feedback from potential users to refine usability and features.
- Conducted on real Android devices for accurate results.

---

NextChapter makes book swapping a seamless, eco-friendly, and user-friendly experience while leveraging cutting-edge mobile and cloud technologies.
