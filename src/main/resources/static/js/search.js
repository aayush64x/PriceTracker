document.getElementById("search-button").addEventListener("click", () => {
    const searchBox = document.getElementById("search-box");
    const keyword = searchBox.value.trim();

    if (!keyword) {
        alert("Please enter a product name");
        return;
    }

    window.location.href = `/search?keyword=${encodeURIComponent(keyword)}`;
});