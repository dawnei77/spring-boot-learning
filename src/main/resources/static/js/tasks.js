// tasks.js — matches your real TaskController: /tasks/complete and
// /tasks/delete both take `id` as a form param (not a path variable),
// and the JSON back from the server uses `title` / `done`.

const csrfToken = document.querySelector('meta[name="_csrf"]').content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

const activeCard = document.getElementById('activeCard');
const completedCard = document.getElementById('completedCard');
const board = document.getElementById('board');
const completedCol = document.getElementById('completedCol');
const emptyState = document.getElementById('emptyState');
const celebrateState = document.getElementById('celebrateState');
const workingState = document.getElementById('workingState');
const completedCountEl = document.getElementById('completedCount');

let showCompleted = false;
let lastCompletedCount = parseInt(completedCountEl.textContent, 10) || 0;

function checkIconPurple() {
  return '<svg class="check-icon" viewBox="0 0 24 24" fill="none" stroke="#8140E3" stroke-width="3.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>';
}

async function apiCall(url, params) {
  const body = params ? new URLSearchParams(params) : null;
  const res = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      [csrfHeader]: csrfToken
    },
    body
  });
  if (!res.ok) throw new Error('Request failed: ' + res.status);
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

function animateRowOut(rowEl, callback) {
  let called = false;
  function finish() {
    if (called) return;
    called = true;
    rowEl.removeEventListener('transitionend', finish);
    callback();
  }
  rowEl.classList.add('row-exit');
  rowEl.addEventListener('transitionend', finish);
  setTimeout(finish, 280);
}

function buildRow(task, isCompletedCard) {
  const row = document.createElement('div');
  row.className = 'task-row';
  row.dataset.id = String(task.id);

  const box = document.createElement(isCompletedCard ? 'span' : 'div');
  if (isCompletedCard) {
    box.className = 'check-wrap';
    box.innerHTML = checkIconPurple();
  } else {
    box.className = 'checkbox';
  }

  const label = document.createElement('div');
  label.className = 'label';
  label.textContent = task.title;

  const del = document.createElement('button');
  del.className = 'delete-btn';
  del.textContent = '✕';
  del.setAttribute('aria-label', 'Delete task');

  row.appendChild(box);
  row.appendChild(label);
  row.appendChild(del);

  wireRow(row, task.id);
  return row;
}

function wireRow(row, taskId) {
  const box = row.querySelector('.checkbox, .check-wrap');
  const label = row.querySelector('.label');
  const del = row.querySelector('.delete-btn');

  const doToggle = (e) => {
    e.stopPropagation();
    animateRowOut(row, () => handleToggle(taskId, row));
  };
  box.addEventListener('click', doToggle);
  label.addEventListener('click', doToggle);
  del.addEventListener('click', (e) => {
    e.stopPropagation();
    animateRowOut(row, () => handleDelete(taskId, row));
  });
}

// Wire up rows the server already rendered on page load.
document.querySelectorAll('.task-row').forEach((row) => {
  wireRow(row, row.dataset.id);
});

async function handleAdd(titleText, inputEl) {
  const trimmed = titleText.trim();
  if (!trimmed) return;
  try {
    const task = await apiCall('/tasks/add', { title: trimmed });
    inputEl.value = '';
    activeCard.appendChild(buildRow(task, false));
    refreshOverallState();
  } catch (err) {
    console.error(err);
  }
}

async function handleToggle(taskId, oldRow) {
  try {
    const task = await apiCall('/tasks/complete', { id: taskId });
    oldRow.remove();
    const destination = task.done ? completedCard : activeCard;
    destination.appendChild(buildRow(task, task.done));
    refreshOverallState();
  } catch (err) {
    console.error(err);
    oldRow.classList.remove('row-exit'); // roll back the visual removal on failure
  }
}

async function handleDelete(taskId, row) {
  try {
    await apiCall('/tasks/delete', { id: taskId });
    row.remove();
    refreshOverallState();
  } catch (err) {
    console.error(err);
    row.classList.remove('row-exit');
  }
}

function refreshOverallState() {
  const activeCount = activeCard.children.length;
  const completedCount = completedCard.children.length;
  const total = activeCount + completedCount;

  emptyState.classList.toggle('visible', total === 0);
  celebrateState.classList.toggle('visible', total > 0 && activeCount === 0);
  workingState.classList.toggle('visible', activeCount > 0);

  if (completedCount !== lastCompletedCount) {
    completedCountEl.classList.remove('pulse');
    void completedCountEl.offsetWidth;
    completedCountEl.classList.add('pulse');
    lastCompletedCount = completedCount;
  }
  completedCountEl.textContent = completedCount;

  const shouldShow = showCompleted && completedCount > 0;
  completedCol.classList.toggle('show', shouldShow);
  board.classList.toggle('single', !shouldShow);
}

function wireInput(inputEl, btnEl, wrapEl) {
  btnEl.addEventListener('click', () => handleAdd(inputEl.value, inputEl));
  inputEl.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') handleAdd(inputEl.value, inputEl);
  });
  inputEl.addEventListener('focus', () => wrapEl.classList.add('focused'));
  inputEl.addEventListener('blur', () => wrapEl.classList.remove('focused'));
}

wireInput(document.getElementById('emptyInput'), document.getElementById('emptyAddBtn'), document.getElementById('emptyInputWrap'));
wireInput(document.getElementById('celebrateInput'), document.getElementById('celebrateAddBtn'), document.getElementById('celebrateInputWrap'));
wireInput(document.getElementById('workingInput'), document.getElementById('workingAddBtn'), document.getElementById('workingInputWrap'));

document.getElementById('toggleCompleted').addEventListener('click', function () {
  showCompleted = !showCompleted;
  this.classList.toggle('on', showCompleted);
  refreshOverallState();
});

const profileBtn = document.getElementById('profileBtn');
const accountMenu = document.getElementById('accountMenu');
profileBtn.addEventListener('click', (e) => {
  e.stopPropagation();
  const isOpen = accountMenu.classList.toggle('open');
  profileBtn.classList.toggle('menu-open', isOpen);
});
document.addEventListener('click', () => {
  accountMenu.classList.remove('open');
  profileBtn.classList.remove('menu-open');
});
accountMenu.addEventListener('click', (e) => e.stopPropagation());

refreshOverallState();