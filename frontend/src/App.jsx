import React, { useState, useEffect } from 'react';
import './App.css';

const API_BASE_URL = 'http://localhost:8080/api/v1/students';

export default function App() {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(false);
  const [serverOnline, setServerOnline] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [pageSize] = useState(5);

  const [notification, setNotification] = useState(null);
  const [editingId, setEditingId] = useState(null);

  const initialForm = {
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    age: '',
    course: ''
  };

  const [formData, setFormData] = useState(initialForm);

  // Fetch students from backend
  const fetchStudents = async (currentPage = page) => {
    setLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}?page=${currentPage}&size=${pageSize}&sortBy=id&sortDir=desc`);
      if (!response.ok) {
        throw new Error(`Server returned HTTP ${response.status}`);
      }
      const data = await response.json();
      setStudents(data.content || []);
      setTotalPages(data.totalPages || 1);
      setTotalElements(data.totalElements || 0);
      setServerOnline(true);
    } catch (err) {
      setServerOnline(false);
      // Only notify if explicit error
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStudents(page);
    const interval = setInterval(() => {
      fetchStudents(page);
    }, 15000);
    return () => clearInterval(interval);
  }, [page]);

  const showNotification = (message, type = 'info') => {
    setNotification({ message, type });
    setTimeout(() => setNotification(null), 5000);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    const payload = {
      firstName: formData.firstName.trim(),
      lastName: formData.lastName.trim(),
      email: formData.email.trim(),
      phone: formData.phone.trim() || null,
      age: formData.age ? parseInt(formData.age, 10) : null,
      course: formData.course.trim() || null
    };

    try {
      const url = editingId ? `${API_BASE_URL}/${editingId}` : API_BASE_URL;
      const method = editingId ? 'PUT' : 'POST';

      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      const resData = await res.json().catch(() => null);

      if (!res.ok) {
        if (resData && resData.fieldErrors) {
          const errors = Object.values(resData.fieldErrors).join(', ');
          throw new Error(errors || resData.message || 'Validation failed');
        }
        throw new Error(resData?.message || `Operation failed (${res.status})`);
      }

      showNotification(
        editingId ? 'Student updated successfully!' : 'Student created successfully!',
        'success'
      );
      setFormData(initialForm);
      setEditingId(null);
      fetchStudents(editingId ? page : 0);
      if (!editingId) setPage(0);
    } catch (err) {
      showNotification(err.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (student) => {
    setEditingId(student.id);
    setFormData({
      firstName: student.firstName || '',
      lastName: student.lastName || '',
      email: student.email || '',
      phone: student.phone || '',
      age: student.age != null ? String(student.age) : '',
      course: student.course || ''
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleCancelEdit = () => {
    setEditingId(null);
    setFormData(initialForm);
  };

  const handleDelete = async (id) => {
    if (!window.confirm(`Are you sure you want to delete student #${id}?`)) return;

    setLoading(true);
    try {
      const res = await fetch(`${API_BASE_URL}/${id}`, {
        method: 'DELETE'
      });

      if (!res.ok && res.status !== 204) {
        throw new Error(`Failed to delete student (Status: ${res.status})`);
      }

      showNotification('Student deleted successfully', 'success');
      fetchStudents(page);
    } catch (err) {
      showNotification(err.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  const fillSampleData = () => {
    const randomNum = Math.floor(100 + Math.random() * 900);
    setFormData({
      firstName: 'Alex',
      lastName: `Taylor${randomNum}`,
      email: `alex.taylor${randomNum}@university.edu`,
      phone: `+1-555-0${randomNum}`,
      age: '22',
      course: 'Computer Science'
    });
  };

  return (
    <div className="container">
      {/* Top Header */}
      <header className="header">
        <div className="title-section">
          <h1>Student Management System</h1>
          <p>Spring Boot 3 REST API + React JavaScript Frontend Demo</p>
        </div>
        <div>
          <span className={`badge ${serverOnline ? 'badge-online' : 'badge-offline'}`}>
            <span className={`dot ${serverOnline ? 'dot-online' : 'dot-offline'}`}></span>
            {serverOnline ? 'Spring Boot: Online (Port 8080)' : 'Spring Boot: Disconnected (Port 8080)'}
          </span>
        </div>
      </header>

      {/* Notifications */}
      {notification && (
        <div className={`alert alert-${notification.type}`}>
          <span>{notification.message}</span>
          <button className="btn-sm btn-secondary" onClick={() => setNotification(null)}>✕</button>
        </div>
      )}

      {/* Backend Offline Help Banner */}
      {!serverOnline && (
        <div className="alert alert-info">
          <div>
            <strong>Backend Connection:</strong> Make sure your Spring Boot backend is running at{' '}
            <code>http://localhost:8080</code>.
          </div>
        </div>
      )}

      {/* Student Form (Create / Edit) */}
      <section className="card">
        <div className="card-title">
          <span>{editingId ? `Edit Student (ID: ${editingId})` : 'Register New Student'}</span>
          {!editingId && (
            <button type="button" className="btn-secondary btn-sm" onClick={fillSampleData}>
              ⚡ Auto-Fill Sample Data
            </button>
          )}
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-grid">
            <div className="form-group">
              <label>First Name *</label>
              <input
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleInputChange}
                required
                placeholder="e.g. Jane"
              />
            </div>

            <div className="form-group">
              <label>Last Name *</label>
              <input
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleInputChange}
                required
                placeholder="e.g. Doe"
              />
            </div>

            <div className="form-group">
              <label>Email *</label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                required
                placeholder="e.g. jane.doe@example.com"
              />
            </div>

            <div className="form-group">
              <label>Phone</label>
              <input
                type="text"
                name="phone"
                value={formData.phone}
                onChange={handleInputChange}
                placeholder="e.g. +1 234 567 8900"
              />
            </div>

            <div className="form-group">
              <label>Age</label>
              <input
                type="number"
                name="age"
                min="1"
                max="150"
                value={formData.age}
                onChange={handleInputChange}
                placeholder="e.g. 21"
              />
            </div>

            <div className="form-group">
              <label>Course</label>
              <input
                type="text"
                name="course"
                value={formData.course}
                onChange={handleInputChange}
                placeholder="e.g. Software Engineering"
              />
            </div>
          </div>

          <div className="form-actions">
            {editingId && (
              <button type="button" className="btn-secondary" onClick={handleCancelEdit}>
                Cancel
              </button>
            )}
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Saving...' : editingId ? 'Update Student' : 'Save Student'}
            </button>
          </div>
        </form>
      </section>

      {/* Student List Table */}
      <section className="card">
        <div className="card-title">
          <span>Student Directory ({totalElements} Total)</span>
          <button
            type="button"
            className="btn-secondary btn-sm"
            onClick={() => fetchStudents(page)}
            disabled={loading}
          >
            🔄 Refresh
          </button>
        </div>

        <div className="table-wrapper">
          {students.length === 0 ? (
            <div className="empty-state">
              {serverOnline
                ? 'No students found. Add your first student using the form above!'
                : 'Unable to load students. Please start the Spring Boot server.'}
            </div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Phone</th>
                  <th>Age</th>
                  <th>Course</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {students.map((student) => (
                  <tr key={student.id}>
                    <td>#{student.id}</td>
                    <td><strong>{student.firstName} {student.lastName}</strong></td>
                    <td>{student.email}</td>
                    <td>{student.phone || '—'}</td>
                    <td>{student.age ?? '—'}</td>
                    <td>{student.course || '—'}</td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                        <button
                          className="btn-secondary btn-sm"
                          onClick={() => handleEdit(student)}
                        >
                          ✏️ Edit
                        </button>
                        <button
                          className="btn-danger btn-sm"
                          onClick={() => handleDelete(student.id)}
                        >
                          🗑️ Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {/* Pagination Controls */}
        {totalPages > 1 && (
          <div className="pagination">
            <span style={{ fontSize: '0.875rem', color: '#64748b' }}>
              Page {page + 1} of {totalPages}
            </span>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <button
                className="btn-secondary btn-sm"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0 || loading}
              >
                Previous
              </button>
              <button
                className="btn-secondary btn-sm"
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1 || loading}
              >
                Next
              </button>
            </div>
          </div>
        )}
      </section>
    </div>
  );
}
