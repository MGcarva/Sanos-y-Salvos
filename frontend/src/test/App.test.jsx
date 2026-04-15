import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../contexts/AuthContext';
import App from '../App';

const renderApp = () => {
    return render(
        <App />
    );
};

describe('App', () => {
    it('renders the navbar', () => {
        renderApp();
        expect(screen.getByText('Sanos y Salvos')).toBeInTheDocument();
    });

    it('renders the home page by default', () => {
        renderApp();
        expect(screen.getByText(/Sanos y Salvos/)).toBeInTheDocument();
    });
});
