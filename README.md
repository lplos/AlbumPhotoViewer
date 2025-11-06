TASK : Album photo viewer

Build a simple Android app that targets a minimum SDK of Android 13, using Jetpack Compose

1. Display a list of albums fetched from the API: (https://jsonplaceholder.typicode.com/albums) <br />
1.1 Dev note: I was unable to use the provided API, because it didn't link to any pictures, only a list of album names and IDs. When fetching, it only returned links that didn't work (e.g. https://via.placeholder.com/600/92c952). Therefore I used a different open source API to accomplish the task: https://picsum.photos/
2. When a user taps on an album, show a system loading indicator and then display a list of photos from that album (e.g.,https://placehold.co)
3. When a user taps on a photo, open it in full-screen mode with pinch-to-zoom enabled
----------------------------------------------------------------------------------------------------------
RESULT:
- The app displays a list of album names with thumbnails
- Clicking an album opens up a system loading indicator for a second, before it displays a list of pictures
- Clicking a photo opens it in full-screen and enables pinch-to-zoom
- The app also has backtrack arrows in the top left corner, to return from photo->album->album list
 
