
async function fetchData(endpoint) {
    const response = await fetch(endpoint);
    if (!response.ok) throw new Error("API error: " + endpoint);
    return await response.json();
}

async function sendForm(endpoint, form) {
    const response = await fetch(endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
        body: new URLSearchParams(new FormData(form))
    });

    const result = await response.json();
    if (!response.ok) throw new Error(result.error || "Request failed.");
    return result;
}

function showMessage(message, isError = false) {
    const element = document.getElementById("formMessage");
    element.textContent = message;
    element.style.color = isError ? "#f87171" : "#4ade80";
}

// Store the latest data so charts can redraw when the window is resized.
let performanceChartData = [];
let problemChartData = [];

function prepareCanvas(canvasId) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return null;

    const width = canvas.clientWidth || 600;
    const height = canvas.clientHeight || 280;
    const ratio = window.devicePixelRatio || 1;

    canvas.width = width * ratio;
    canvas.height = height * ratio;

    const context = canvas.getContext("2d");
    context.setTransform(ratio, 0, 0, ratio, 0, 0);

    return { canvas, context, width, height };
}

function drawEmptyMessage(context, width, height, message) {
    context.fillStyle = "#94a3b8";
    context.font = "14px Arial";
    context.textAlign = "center";
    context.fillText(message, width / 2, height / 2);
}

function drawAcademicChart(students) {
    const setup = prepareCanvas("academicChart");
    if (!setup) return;

    const { context: ctx, width, height } = setup;
    ctx.clearRect(0, 0, width, height);

    if (!students.length) {
        drawEmptyMessage(ctx, width, height, "Add marks to see academic performance.");
        return;
    }

    const data = [...students]
        .sort((a, b) => Number(b.percentage) - Number(a.percentage))
        .slice(0, 8);

    const left = 105;
    const right = 42;
    const top = 15;
    const barHeight = 18;
    const gap = 13;
    const availableWidth = width - left - right;

    data.forEach((student, index) => {
        const y = top + index * (barHeight + gap);
        const percentage = Math.max(0, Math.min(100, Number(student.percentage) || 0));

        ctx.fillStyle = "#cbd5e1";
        ctx.font = "12px Arial";
        ctx.textAlign = "right";
        ctx.fillText(
            String(student.name || "Student").slice(0, 15),
            left - 10,
            y + 13
        );

        ctx.fillStyle = "#25334a";
        ctx.fillRect(left, y, availableWidth, barHeight);

        const barWidth = availableWidth * percentage / 100;
        const gradient = ctx.createLinearGradient(left, 0, left + availableWidth, 0);
        gradient.addColorStop(0, "#6366f1");
        gradient.addColorStop(1, "#22d3ee");

        ctx.fillStyle = gradient;
        ctx.fillRect(left, y, barWidth, barHeight);

        ctx.fillStyle = "#e2e8f0";
        ctx.textAlign = "left";
        ctx.fillText(percentage.toFixed(1) + "%", left + barWidth + 7, y + 13);
    });
}

function drawTopicChart(problems) {
    const setup = prepareCanvas("topicChart");
    if (!setup) return;

    const { context: ctx, width, height } = setup;
    ctx.clearRect(0, 0, width, height);

    if (!problems.length) {
        drawEmptyMessage(ctx, width, height, "Add DSA problems to see topic progress.");
        return;
    }

    const topics = {};

    problems.forEach(problem => {
        const topic = problem.topic || "Other";

        if (!topics[topic]) {
            topics[topic] = { total: 0, solved: 0 };
        }

        topics[topic].total++;
        if (problem.solved) topics[topic].solved++;
    });

    const data = Object.entries(topics);
    const left = 105;
    const right = 35;
    const top = 15;
    const barHeight = 18;
    const gap = 14;
    const availableWidth = width - left - right;

    data.forEach(([topic, values], index) => {
        const y = top + index * (barHeight + gap);
        const solvedWidth = availableWidth * values.solved / values.total;
        const unsolvedWidth = availableWidth - solvedWidth;

        ctx.fillStyle = "#cbd5e1";
        ctx.font = "12px Arial";
        ctx.textAlign = "right";
        ctx.fillText(topic.slice(0, 15), left - 10, y + 13);

        ctx.fillStyle = "#25334a";
        ctx.fillRect(left, y, availableWidth, barHeight);

        ctx.fillStyle = "#22c55e";
        ctx.fillRect(left, y, solvedWidth, barHeight);

        ctx.fillStyle = "#64748b";
        ctx.fillRect(left + solvedWidth, y, unsolvedWidth, barHeight);

        ctx.fillStyle = "#e2e8f0";
        ctx.textAlign = "left";
        ctx.fillText(
            values.solved + "/" + values.total,
            left + availableWidth + 7,
            y + 13
        );
    });
}

function drawDifficultyChart(problems) {
    const setup = prepareCanvas("difficultyChart");
    if (!setup) return;

    const { context: ctx, width, height } = setup;
    ctx.clearRect(0, 0, width, height);

    const categories = [
        { name: "Easy", color: "#22c55e" },
        { name: "Medium", color: "#f59e0b" },
        { name: "Hard", color: "#f87171" }
    ];

    const counts = categories.map(category => ({
        ...category,
        count: problems.filter(problem =>
            String(problem.difficulty || "").toLowerCase() === category.name.toLowerCase()
        ).length
    }));

    const total = counts.reduce((sum, item) => sum + item.count, 0);

    if (total === 0) {
        drawEmptyMessage(ctx, width, height, "Add DSA problems to see difficulty analysis.");
        return;
    }

    const centerX = width / 2;
    const centerY = Math.min(height / 2, 115);
    const radius = Math.min(75, height * 0.32, width * 0.22);
    let startAngle = -Math.PI / 2;

    counts.forEach(item => {
        if (item.count === 0) return;

        const sliceAngle = (item.count / total) * Math.PI * 2;

        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.arc(centerX, centerY, radius, startAngle, startAngle + sliceAngle);
        ctx.closePath();
        ctx.fillStyle = item.color;
        ctx.fill();

        startAngle += sliceAngle;
    });

    // Donut hole
    ctx.beginPath();
    ctx.arc(centerX, centerY, radius * 0.58, 0, Math.PI * 2);
    ctx.fillStyle = "#151f30";
    ctx.fill();

    ctx.fillStyle = "#f8fafc";
    ctx.font = "bold 20px Arial";
    ctx.textAlign = "center";
    ctx.fillText(String(total), centerX, centerY + 7);

    // Legend
    const legendY = Math.min(height - 35, centerY + radius + 25);
    const itemWidth = Math.min(110, (width - 20) / 3);
    const legendStart = (width - itemWidth * 3) / 2;

    counts.forEach((item, index) => {
        const x = legendStart + index * itemWidth;

        ctx.fillStyle = item.color;
        ctx.fillRect(x, legendY - 10, 10, 10);

        ctx.fillStyle = "#cbd5e1";
        ctx.font = "12px Arial";
        ctx.textAlign = "left";
        ctx.fillText(item.name + ": " + item.count, x + 15, legendY);
    });
}

function redrawCharts() {
    drawAcademicChart(performanceChartData);
    drawTopicChart(problemChartData);
    drawDifficultyChart(problemChartData);
}

async function loadStats() {
    const stats = await fetchData("/api/stats");

    document.getElementById("studentsCount").textContent = stats.students;
    document.getElementById("totalProblems").textContent = stats.totalProblems;
    document.getElementById("solvedProblems").textContent = stats.solved;
    document.getElementById("completion").textContent =
        Number(stats.completion).toFixed(1) + "%";

    document.getElementById("progressText").textContent =
        Number(stats.completion).toFixed(1) + "%";

    document.getElementById("progressFill").style.width =
        Math.max(0, Math.min(100, Number(stats.completion))) + "%";

    document.getElementById("solvedSummary").textContent = stats.solved;
    document.getElementById("unsolvedSummary").textContent = stats.unsolved;
}

async function loadStudents() {
    const table = document.getElementById("studentTable");
    const select = document.getElementById("marksStudent");

    try {
        const students = await fetchData("/api/students");

        table.innerHTML = "";
        select.innerHTML = '<option value="">Select student</option>';

        if (students.length === 0) {
            table.innerHTML = '<tr><td colspan="2">No students registered.</td></tr>';
            return;
        }

        students.forEach(student => {
            const row = document.createElement("tr");
            const idCell = document.createElement("td");
            const nameCell = document.createElement("td");

            idCell.textContent = student.id;
            nameCell.textContent = student.name ?? "";
            row.append(idCell, nameCell);
            table.appendChild(row);

            const option = document.createElement("option");
            option.value = student.id;
            option.textContent = student.id + " - " + student.name;
            select.appendChild(option);
        });
    } catch (error) {
        console.error(error);
        table.innerHTML = '<tr><td colspan="2">Unable to load students.</td></tr>';
    }
}

async function loadProblems() {
    const table = document.getElementById("problemTable");

    try {
        const problems = await fetchData("/api/problems");
        problemChartData = problems;

        table.innerHTML = "";

        if (problems.length === 0) {
            table.innerHTML = '<tr><td colspan="5">No DSA problems added.</td></tr>';
        } else {
            problems.forEach(problem => {
                const row = document.createElement("tr");

                [problem.title, problem.topic, problem.difficulty].forEach(value => {
                    const cell = document.createElement("td");
                    cell.textContent = value ?? "";
                    row.appendChild(cell);
                });

                const statusCell = document.createElement("td");
                const badge = document.createElement("span");
                badge.className = problem.solved ? "status solved" : "status unsolved";
                badge.textContent = problem.solved ? "Solved" : "Unsolved";
                statusCell.appendChild(badge);
                row.appendChild(statusCell);

                const actionCell = document.createElement("td");
                const button = document.createElement("button");

                button.type = "button";
                button.textContent = problem.solved ? "Mark Unsolved" : "Mark Solved";
                button.addEventListener("click", () =>
                    updateProblemStatus(problem.id, !problem.solved)
                );

                actionCell.appendChild(button);
                row.appendChild(actionCell);
                table.appendChild(row);
            });
        }

        drawTopicChart(problemChartData);
        drawDifficultyChart(problemChartData);
    } catch (error) {
        console.error(error);
        table.innerHTML = '<tr><td colspan="5">Unable to load DSA problems.</td></tr>';
    }
}

async function updateProblemStatus(problemId, solved) {
    try {
        const body = new URLSearchParams({ problemId, solved });

        const response = await fetch("/api/problems/status", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
            body
        });

        const result = await response.json();
        if (!response.ok) throw new Error(result.error || "Could not update status.");

        showMessage("DSA problem status updated.");
        await loadDashboard();
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function loadPerformance() {
    const table = document.getElementById("performanceTable");

    try {
        const students = await fetchData("/api/performance");
        performanceChartData = students;

        table.innerHTML = "";

        if (students.length === 0) {
            table.innerHTML = '<tr><td colspan="6">No performance records available.</td></tr>';
        } else {
            students.forEach(student => {
                const row = document.createElement("tr");

                const values = [
                    student.id,
                    student.name,
                    student.totalMarks,
                    student.maxMarks,
                    Number(student.percentage).toFixed(1) + "%",
                    student.grade
                ];

                values.forEach(value => {
                    const cell = document.createElement("td");
                    cell.textContent = value ?? "";
                    row.appendChild(cell);
                });

                table.appendChild(row);
            });
        }

        drawAcademicChart(performanceChartData);
    } catch (error) {
        console.error(error);
        table.innerHTML = '<tr><td colspan="6">Unable to load academic performance.</td></tr>';
    }
}

async function loadTopper() {
    const container = document.getElementById("topperDetails");

    try {
        const topper = await fetchData("/api/topper");
        container.innerHTML = "";

        if (topper === null) {
            container.textContent = "No marks have been entered yet.";
            return;
        }

        const name = document.createElement("h3");
        name.textContent = topper.name;

        const percentage = document.createElement("p");
        percentage.textContent =
            "Percentage: " + Number(topper.percentage).toFixed(1) + "%";

        const grade = document.createElement("p");
        grade.textContent = "Grade: " + topper.grade;

        const id = document.createElement("p");
        id.textContent = "Student ID: " + topper.id;

        container.append(name, percentage, grade, id);
    } catch (error) {
        console.error(error);
        container.textContent = "Unable to load topper information.";
    }
}

async function loadDashboard() {
    try {
        await loadStats();
    } catch (error) {
        console.error("Statistics error:", error);
    }

    await Promise.all([
        loadStudents(),
        loadProblems(),
        loadPerformance(),
        loadTopper()
    ]);
}

// Student form
document.getElementById("studentForm").addEventListener("submit", async event => {
    event.preventDefault();
    const form = event.currentTarget;

    try {
        const result = await sendForm("/api/students", form);
        form.reset();
        showMessage(result.message);
        await loadDashboard();
    } catch (error) {
        showMessage(error.message, true);
    }
});

// Marks form
document.getElementById("marksForm").addEventListener("submit", async event => {
    event.preventDefault();
    const form = event.currentTarget;

    const obtained = Number(document.getElementById("marks").value);
    const maximum = Number(document.getElementById("maxMarks").value);

    if (obtained < 0 || maximum <= 0 || obtained > maximum) {
        showMessage("Marks must be between 0 and the maximum marks.", true);
        return;
    }

    try {
        const result = await sendForm("/api/marks", form);
        form.reset();
        showMessage(result.message);
        await loadDashboard();
    } catch (error) {
        showMessage(error.message, true);
    }
});

// DSA problem form
document.getElementById("problemForm").addEventListener("submit", async event => {
    event.preventDefault();
    const form = event.currentTarget;

    try {
        const result = await sendForm("/api/problems", form);
        form.reset();
        showMessage(result.message);
        await loadDashboard();
    } catch (error) {
        showMessage(error.message, true);
    }
});

// Redraw charts when browser size changes.
let resizeTimer;
window.addEventListener("resize", () => {
    clearTimeout(resizeTimer);
    resizeTimer = setTimeout(redrawCharts, 150);
});

loadDashboard();
