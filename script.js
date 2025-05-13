// Function to update vote counts and winning chances on the page
function updateVotes(data) {
    console.log('Raw data received:', JSON.stringify(data));
    const votesA = data['candidatea'] || 0;
    const votesB = data['candidateb'] || 0;
    const votesC = data['candidatec'] || 0;
    console.log('Parsed votes - A:', votesA, 'B:', votesB, 'C:', votesC);

    // Update vote counts
    document.getElementById('votes-A').innerText = votesA;
    document.getElementById('votes-B').innerText = votesB;
    document.getElementById('votes-C').innerText = votesC;
    console.log('Updated HTML - A:', document.getElementById('votes-A').innerText,
                'B:', document.getElementById('votes-B').innerText,
                'C:', document.getElementById('votes-C').innerText);

    // Calculate and display winning chances
    const totalVotes = votesA + votesB + votesC;
    if (totalVotes > 0) { // Only calculate if there are votes
        const chanceA = ((votesA / totalVotes) * 100).toFixed(1);
        const chanceB = ((votesB / totalVotes) * 100).toFixed(1);
        const chanceC = ((votesC / totalVotes) * 100).toFixed(1);
        document.getElementById('chance-A').innerText = `${chanceA}%`;
        document.getElementById('chance-B').innerText = `${chanceB}%`;
        document.getElementById('chance-C').innerText = `${chanceC}%`;
        document.getElementById('winning-chances').style.display = 'block';
    }
}

// Function to send a vote to the server, disable buttons, and show notification
function vote(candidate) {
    console.log('Voting for:', candidate);
    fetch('/vote', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `candidate=${encodeURIComponent(candidate)}`
    })
    .then(response => {
        console.log('Fetch response status:', response.status);
        if (!response.ok) throw new Error('Network response was not ok ' + response.status);
        return response.json();
    })
    .then(data => {
        updateVotes(data);
        console.log('Vote successful:', JSON.stringify(data));
        // Disable all buttons
        document.querySelectorAll('button').forEach(button => button.disabled = true);
        // Show notification
        const notification = document.getElementById('notification');
        notification.innerText = 'Thanks for voting!';
        notification.style.display = 'block';
    })
    .catch(error => console.error('Error voting:', error));
}

// Fetch initial vote counts when the page loads
fetch('/results')
    .then(response => {
        console.log('Results fetch status:', response.status);
        if (!response.ok) throw new Error('Network response was not ok ' + response.status);
        return response.json();
    })
    .then(data => {
        updateVotes(data);
        console.log('Initial results:', JSON.stringify(data));
    })
    .catch(error => console.error('Error fetching results:', error));